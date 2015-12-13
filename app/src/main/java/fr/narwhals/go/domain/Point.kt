package fr.narwhals.go.domain

import java.io.Serializable

class Point(val x: Int, val y: Int) : Serializable {

    override fun equals(obj: Any?): Boolean {
        if (obj is Point) {
            return obj.x == x && obj.y == y
        } else {
            return false
        }
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    companion object {
        val NO_KO = Point(-2, -2)
    }
}
