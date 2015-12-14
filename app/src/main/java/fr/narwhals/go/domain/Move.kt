package fr.narwhals.go.domain

import java.io.IOException
import java.io.Serializable

import fr.narwhals.go.sgf.SgfWriter

class Move(var stone: Stone? // B/W
) : Serializable {
    var ko = Point.NO_KO // KO
    @Transient var comment = "" // C
    @Transient var labels: List<Label>? = null // LB
    @Transient var circles: List<Point>? = null // CR
    @Transient var squares: List<Point>? = null // SQ
    @Transient var triangles: List<Point>? = null // TR
    @Transient var empties: List<Point>? = null // AE
    @Transient var marked: List<Point>? = null // MA

    @Throws(IOException::class)
    fun toSgf(writer: SgfWriter) {
        writer.write(stone!!.color!!.key, stone!!.point)
        if (ko !== Point.NO_KO) {
            writer.write("KO", ko)
        }
    }

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
