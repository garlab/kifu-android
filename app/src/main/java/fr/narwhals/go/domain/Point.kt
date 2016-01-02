package fr.narwhals.go.domain

import java.io.Serializable

data class Point(val x: Int, val y: Int) : Serializable {

    companion object {
        @JvmField val PASS  = Point(-1, -1)
        @JvmField val NO_KO = Point(-2, -2)
    }

    override fun toString() = when (this) {
        PASS -> ""
        else -> String(charArrayOf('a'.plus(x - 1), 'a'.plus(y - 1)))
    }
}
