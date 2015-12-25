package fr.narwhals.go.domain

import fr.narwhals.go.domain.Section.SColor
import java.io.Serializable

class Player(val color: SColor, val name: String, val ai: Boolean) : Serializable {

    override fun toString(): String {
        return name
    }
}
