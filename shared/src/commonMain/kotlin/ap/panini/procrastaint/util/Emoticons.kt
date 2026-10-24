package ap.panini.procrastaint.util

object Emoticons {
    val ALL: List<String> = listOf(
        "(•‿•)",
        "(^_^)",
        ";-;",
        "(｡◕‿◕｡)",
        "¯\\_(ツ)_/¯",
        "(>_<)",
        "(¬_¬)",
        "(✿◠‿◠)",
        "(ง'̀-'́)ง",
        "(◕‿◕✿)",
        "(o_o)",
        "(^o^)",
        "( ＾◡＾)",
        "(•_•)",
        "(¬‿¬)",
        "(~_~)",
        "(*^▽^*)",
        "(=^･^=)"
    )

    fun random(): String = ALL.random()
}
