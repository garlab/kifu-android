package fr.narwhals.go.domain


class Liberty(color: Section.SColor, point: Point, goban: Goban) : Section(color, point, goban) {

    var territory: Territory? = null

    init {
        territory = null
    }

    fun hasTerritory(): Boolean {
        return territory != null
    }
}
