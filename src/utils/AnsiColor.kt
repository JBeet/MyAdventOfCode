package utils

enum class AnsiColor(private val fg: Int, private val bg: Int) {
    BLACK(30, 40), RED(31, 41), GREEN(32, 42), YELLOW(33, 43), BLUE(34, 44),
    MAGENTA(35, 45), CYAN(36, 46), WHITE(37, 47),
    BRIGHT_BLACK(90, 100), BRIGHT_RED(91, 101), BRIGHT_GREEN(92, 102), BRIGHT_YELLOW(93, 103), BRIGHT_BLUE(94, 104),
    BRIGHT_MAGENTA(95, 105), BRIGHT_CYAN(96, 106), BRIGHT_WHITE(97, 107),

    DEFAULT(39, 49), RESET(0, 0);

    fun fgCode(): String = "\u001b[${fg}m"
    fun bgCode(): String = "\u001b[${bg}m"
}
