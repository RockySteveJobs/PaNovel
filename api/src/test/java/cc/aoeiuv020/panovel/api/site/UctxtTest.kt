package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.09-20:25:05.
 */
class UctxtTest : BaseNovelContextText(Uctxt::class) {
    @Test
    fun search() {
        search("都市")
        search("武血至尊", "御史大夫", "1/1132")
        search("龙王传说", "唐家三少", "11/11798")
    }

    @Test
    fun detail() {
        detail("1/1132", "1/1132", "武血至尊", "御史大夫",
                null,
                "聚天地之根本立身；\n" +
                        "取日月之精华所用；\n" +
                        "以体魄之精髓换血脉之升华，是为通天法则；\n" +
                        "然，法则之上乃有血脉至尊成就永生王者。",
                "2016-05-02 19:10:00")
        detail("11/11798", "11/11798", "龙王传说", "唐家三少",
                null,
                "伴随着魂导科技的进步，斗罗大陆上的人类征服了海洋，又发现了两片大陆。" +
                        "魂兽也随着人类魂师的猎杀无度走向灭亡，" +
                        "沉睡无数年的魂兽之王在星斗大森林最后的净土苏醒，它要带领仅存的族人，向人类复仇！" +
                        "唐舞麟立志要成为一名强大的魂师，可当武魂觉醒时，苏醒的，却是……" +
                        "旷世之才，龙王之争，我们的龙王传说，将由此开始。",
                "2018-06-09 17:02:00")
    }

    @Test
    fun chapters() {
        chapters("1/1132", "001章 长大后，我要嫁给你", "1/1132/349065", null,
                "344章 多智似妖之二", "1/1132/4445378", "2016-05-02 19:10:00",
                344)
        chapters("11/11798", "楔子", "11/11798/3357952", null,
                "第一千八百二十九章 融合，父亲的血脉", "11/11798/10128252", "2018-06-09 17:02:00",
                1841)
    }

    @Test
    fun content() {
        content("1/1132/349065",
                "火，大火，漫天大火。",
                "<rf=p://.qidi.>起点中文网.qidi.欢迎广大书友光临阅读，最新、最快、最火的连载作品尽在起点原创！</><>手机用户请到.qidi.阅读。</>",
                39)
        content("11/11798/10128252",
                "“是它、是它。百万年，是它。它居然还活着，它回来了……”这是一个惊恐无比的念头，也唯有这一个念头留下来。",
                "而在他身下的大海也开始出现了变化。刚开始的时候，只是一小片的蓝色出现，但很快，这片蓝色就开始朝着四面八方蔓延开来，向远方蔓延。",
                50)
    }

}