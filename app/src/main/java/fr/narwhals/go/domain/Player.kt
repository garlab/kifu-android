package fr.narwhals.go.domain

import java.io.IOException

import fr.narwhals.go.ai.AI
import fr.narwhals.go.domain.Section.SColor
import fr.narwhals.go.sgf.SgfWriter
import java.io.Serializable

class Player(val color: SColor, val name: String) : Serializable {
    public var ai: AI? = null

    @Throws(IOException::class)
    fun toSgf(writer: SgfWriter) {
        writer.write("P" + color.key, name)
    }

    val isAI: Boolean
        get() = ai != null

    override fun toString(): String {
        return name
    }
}
