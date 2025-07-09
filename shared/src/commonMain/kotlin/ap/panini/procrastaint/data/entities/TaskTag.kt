package ap.panini.procrastaint.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.random.Random

@Entity
data class TaskTag(

    val displayTitle: String,

    @ColumnInfo(index = true)
    val title: String,

    val info: String,

    val color: String,

    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0,
) {
    constructor(title: String) : this(title.replace('-', ' '), title, "", "")

    fun toRgb() = hexToRgb(color)

    fun toRgbOrNull() = try {
        hexToRgb(color)
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val HEX_CHARS = "0123456789ABCDEF"
        private const val BASE16 = 16

        const val HUE = 256

        private const val HEX_BASE = 16
        private const val HEX_PREFIX = "#"
        private const val HEX_LENGTH = 7
        private const val HEX_R_START = 1
        private const val HEX_R_END = 2
        private const val HEX_G_START = 3
        private const val HEX_G_END = 4
        private const val HEX_B_START = 5
        private const val HEX_B_END = 6
        private const val HEX_VALUE_OFFSET = 10

        fun rgbToHex(r: Int, g: Int, b: Int): String {
            return "#" + intToHex(r) + intToHex(g) + intToHex(b)
        }

        private fun intToHex(value: Int): String {
            val high = value / BASE16
            val low = value % BASE16
            return "${HEX_CHARS[high]}${HEX_CHARS[low]}"
        }

        fun generateRandomColor() =
            rgbToHex(Random.nextInt(HUE), Random.nextInt(HUE), Random.nextInt(HUE))

        fun hexToRgb(hex: String): Triple<Int, Int, Int> {
            fun hexCharToInt(c: Char): Int {
                return when (c.uppercaseChar()) {
                    in '0'..'9' -> c - '0'
                    in 'A'..'F' -> c - 'A' + HEX_VALUE_OFFSET
                    else -> throw IllegalArgumentException("Invalid hex character: $c")
                }
            }

            fun hexPairToInt(c1: Char, c2: Char): Int {
                return hexCharToInt(c1) * HEX_BASE + hexCharToInt(c2)
            }

            require((hex.startsWith(HEX_PREFIX) && hex.length == HEX_LENGTH))

            val r = hexPairToInt(hex[HEX_R_START], hex[HEX_R_END])
            val g = hexPairToInt(hex[HEX_G_START], hex[HEX_G_END])
            val b = hexPairToInt(hex[HEX_B_START], hex[HEX_B_END])

            return Triple(r, g, b)
        }
    }

    fun generateTag() = "#$title"
}

@Entity(
    primaryKeys = ["taskId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TaskInfo::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TaskTag::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long,
)
