package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.base.JsoupNovelContext
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 * Created by AoEiuV020 on 2018.05.10-18:11:57.
 */
class Qlyx : DslJsoupNovelContext() { init {
    site {
        name = "齐鲁文学"
        baseUrl = "http://www.76wx.com"
        logo = "http://www.76wx.com/images/book_logo.png"
    }
    search {
        get {
            // 加上&page=1可以避开搜索时间间隔的限制，
            // 之前是通过不加载cookies避开搜索时间间隔的限制，
            url = "/modules/article/search.php?searchtype=articlename&searchkey=${gbk(it)}&page=1"
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/book/")) {
                TODO()
            } else {
                items("#main > table > tbody > tr:not(:nth-child(1))") {
                    name("td:nth-child(1) > a")
                    author("td:nth-child(3)")
                }
            }
        }
    }
    detailTemplate = "/book/%s/"
    detail {
        document {
            val eMaininfo = element(query = "#maininfo")
            val eInfo = element(query = "#info", parent = eMaininfo)
            novel {
                name("> h1", eInfo)
                author("> p:nth-child(2)", eInfo)
            }
            image("#fmimg > img")
            introduction("#intro > p:not(:nth-last-child(1))")
            update("> p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss") {
                it.text().pick("更新时间：(.*)").first()
            }
        }
    }
    chapters {
        // 开头 9 章可能是网站上显示的最新章节，和列表最后重复，
        // 但也可能没有这重复的 9 章，
        val list = document {
            items("#list > dl > dd > a")
        }
        var index = 0
        // 以防万一，
        if (list.size == 1) return@chapters list
        // 倒序列表判断是否重复章节，
        val reversedList = list.asReversed()
        list.dropWhile {
            (it == reversedList[index]).also { ++index }
        }
    }
    // http://www.76wx.com/book/161/892418.html
    contentTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

// 这类已经不用了，搞定dsl就删除，
@Suppress("ClassName", "unused")
class _Qlyx : JsoupNovelContext() {
    override val site: NovelSite = NovelSite(
            name = "齐鲁文学",
            baseUrl = "http://www.76wx.com",
            logo = "http://www.76wx.com/images/book_logo.png"
    )

    private fun isDetail(url: String) = URL(url).path.startsWith("/book/")
    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "GBK")
        // 加上&page=1可以避开搜索时间间隔的限制，
        // 之前是通过不加载cookies避开搜索时间间隔的限制，
        return connect(absUrl("/modules/article/search.php?searchtype=articlename&searchkey=$key&page=1"))
    }

    override fun searchNovelName(name: String): List<NovelItem> {
        val root = parse(connectByNovelName(name))
        // 搜索结果可能直接跳到详情页，
        return if (isDetail(root.location())) {
            val detail = getNovelDetail(root)
            listOf(detail.novel)
        } else {
            getSearchResultList(root)
        }
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        val elements = root.requireElements(name = TAG_SEARCH_RESULT_LIST, query = "#main > table > tbody > tr:not(:nth-child(1))")
        return elements.map {
            val (name, bookId) = it.requireElement(query = "td:nth-child(1) > a") { a ->
                a.text() to findBookId(a.href())
            }
            val author = it.requireElement(query = "td:nth-child(3)", name = TAG_AUTHOR_NAME) {
                it.text()
            }
            NovelItem(this, name, author, bookId)
        }
    }

    override fun getNextPage(root: Document): String? {
        /*
        <a href="/1_1021/4867306.html" target="_blank"> 第一千一百四十七章 回地底</a>
         */
        return root.getElement("#pagelink > a.next")?.href()
    }

    override val detailTemplate: String?
        get() = "/book/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val eMaininfo = root.requireElement(query = "#maininfo")
        val eInfo = eMaininfo.requireElement(query = "#info")
        val img = root.requireElement(query = "#fmimg > img", name = TAG_IMAGE) {
            it.absSrc()
        }
        val name = eInfo.requireElement(query = "> h1", name = TAG_NOVEL_NAME) {
            it.text()
        }
        val (author) = eInfo.requireElement(query = "> p:nth-child(2)", name = TAG_AUTHOR_NAME) {
            it.text().pick("作    者：(\\S*)")
        }
        val intro = root.getElements("#intro > p:not(:nth-last-child(1))") {
            it.ownTextList().joinToString("\n")
        }.toString()

        val update = eInfo.getElement(query = "> p:nth-child(4)") {
            val (updateString) = it.text().pick("更新时间：(.*)")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        }

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElement(query = "#list > dl") {
            it.children()
                    // 删除第一个dt,
                    .drop(1)
                    // 删除第二个dt前的dd,
                    .dropWhile {
                        it.tagName() != "dt"
                    }
                    // 删除第二个dt,
                    .drop(1)
        }.map { dd ->
            /*
            <a href="4370292.html">第四十一章 牛头怪</a>
             */
            val a = dd.requireElement(query = "> a", name = TAG_CHAPTER_LINK)
            NovelChapter(a.text(), a.path())
        }
    }

    // http://www.76wx.com/book/161/892418.html
    override val contentTemplate: String?
        get() = "/book/%s.html"

    override fun getNovelText(root: Document): NovelText {
        val content = root.requireElement(query = "#content", name = TAG_CONTENT)
        return NovelText(content.ownTextList())
    }
}