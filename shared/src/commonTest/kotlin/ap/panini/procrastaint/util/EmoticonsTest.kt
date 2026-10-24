package ap.panini.procrastaint.util

import kotlin.test.Test
import kotlin.test.assertTrue

class EmoticonsTest {
    @Test
    fun testEmoticonsPoolIsNotEmpty() {
        assertTrue(Emoticons.ALL.isNotEmpty())
        assertTrue(Emoticons.ALL.contains("(•‿•)"))
        assertTrue(Emoticons.ALL.contains(";-;"))
    }

    @Test
    fun testRandomEmoticonReturnsItemFromPool() {
        val emoticon = Emoticons.random()
        assertTrue(Emoticons.ALL.contains(emoticon))
    }
}
