package fr.narwhals.go.domain

import java.io.Serializable

data class Point(val x: Int, val y: Int) : Serializable {

    companion object {
        val NO_KO = Point(-2, -2)
    }

    override fun toString() = String(charArrayOf('a'.plus(x - 1), 'a'.plus(y - 1)))
}
