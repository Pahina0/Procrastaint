package ap.panini.procrastaint.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
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
    } catch (e: Exception) {
        null
    }

    companion object {
        fun rgbToHex(r: Int, g: Int, b: Int): String {
            return "#" + intToHex(r) + intToHex(g) + intToHex(b)
        }

        private fun intToHex(value: Int): String {
            val hexChars = "0123456789ABCDEF"
            val high = value / 16
            val low = value % 16
            return "${hexChars[high]}${hexChars[low]}"
        }

        fun generateRandomColor() =
            rgbToHex(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

        fun hexToRgb(hex: String): Triple<Int, Int, Int> {
            fun hexCharToInt(c: Char): Int {
                return when (c.uppercaseChar()) {
                    in '0'..'9' -> c - '0'
                    in 'A'..'F' -> c - 'A' + 10
                    else -> throw IllegalArgumentException("Invalid hex character: $c")
                }
            }

            fun hexPairToInt(c1: Char, c2: Char): Int {
                return hexCharToInt(c1) * 16 + hexCharToInt(c2)
            }

            if (!hex.startsWith("#") || hex.length != 7) {
                throw IllegalArgumentException("Hex color must be in the format #RRGGBB invalid Hex of $hex")
            }

            val r = hexPairToInt(hex[1], hex[2])
            val g = hexPairToInt(hex[3], hex[4])
            val b = hexPairToInt(hex[5], hex[6])

            return Triple(r, g, b)
        }
    }

    fun generateTag() = "#$title"
}

@Entity(primaryKeys = ["taskId", "tagId"])
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long,
)
