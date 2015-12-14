package fr.narwhals.go.domain

import java.io.Serializable

/* Base commune aux Stones et Liberties */
open class Section(var color: Section.SColor?, val point: Point?, protected val goban: Goban?) : Serializable {

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

    val opponentColor: SColor
        get() = color!!.opponentColor

    fun hasGoban(): Boolean {
        return goban != null
    }

    fun add() {
        goban!!.set(this)
    }

    override fun toString(): String {
        return "[point=$point, color=$color]"
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true
        if (obj == null)
            return false
        if (javaClass != obj.javaClass)
            return false
        val other = obj as Section?
        if (point == null) {
            if (other!!.point != null)
                return false
        } else if (point != other!!.point)
            return false
        return true
    }
}
