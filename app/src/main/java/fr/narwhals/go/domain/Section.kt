package fr.narwhals.go.domain

import java.io.Serializable

/* Base commune aux Stones et Liberties */
open class Section(var color: Section.SColor, val point: Point, protected val goban: Goban) : Serializable {

    enum class SColor {
        BLACK, WHITE, BORDER, SHARED, NONE;

        val opponentColor: SColor
            get() {
                when (this) {
                    BLACK -> return SColor.WHITE
                    WHITE -> return SColor.BLACK
                    else -> return SColor.NONE
                }
            }

        val key: String
            get() = if (this == BLACK) "B" else "W"
    }

    override fun toString(): String {
        return "[point=$point, color=$color]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (javaClass != other.javaClass)
            return false
        val other = other as Section
        if (point != other.point)
            return false
        return true
    }

    override fun hashCode(): Int{
        var result = color.hashCode()
        result += 31 * result + point.hashCode()
        return result
    }
}
