package fr.narwhals.go.domain

import java.io.Serializable

class Move(var stone: Stone?) : Serializable {
    var ko = Point.NO_KO // KO
    var comment = "" // C
    var labels: List<Label>? = null // LB
    var circles: List<Point>? = null // CR
    var squares: List<Point>? = null // SQ
    var triangles: List<Point>? = null // TR
    var empties: List<Point>? = null // AE
    var marked: List<Point>? = null // MA

    fun hasCircles(): Boolean {
        return this.circles != null
    }

    fun hasSquares(): Boolean {
        return this.squares != null
    }

    fun hasTriangles(): Boolean {
        return this.triangles != null
    }
}
