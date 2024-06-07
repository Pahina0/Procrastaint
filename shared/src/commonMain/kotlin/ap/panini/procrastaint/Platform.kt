package ap.panini.procrastaint

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform