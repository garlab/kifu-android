package fr.narwhals.go.domain

import java.util.ArrayList
import java.util.LinkedList

class Stone(val round: Int, color: Section.SColor, point: Point, goban: Goban) : Section(color, point, goban) {

    /**
     * Utilisé par StoneGroup lors des merge
     */
    var stoneGroup: StoneGroup? = null

    // TODO: move into Move
    val capturedStones = LinkedList<Stone>()

    // Liberties

    /**
     * Return the list of liberties adjacent to the stone.
     */
    val liberties: MutableList<Liberty>
        get() {
            val liberties = ArrayList<Liberty>(4)
            for (neighbor in goban.getNeighbors(point)) {
                if (neighbor is Liberty) {
                    liberties.add(neighbor)
                }
            }
            return liberties
        }

    /**
     * Return the list of liberties that will be adjacent to the group, once this stone is placed.
     */
    val actualLiberties: List<Liberty>
        get() {
            val liberties = liberties
            for (neighbor in sameColorGroupNeighbors) {
                for (liberty in neighbor.liberties) {
                    // TODO: replace by a Set
                    if (!liberties.contains(liberty)) {
                        liberties.add(liberty)
                    }
                }
            }
            liberties.remove(goban.getLiberty(point))
            return liberties
        }

    /**
     * Return unique liberties for each adjacent group.
     */
    val actualNeighborLiberties: List<Liberty>
        get() {
            val liberties = LinkedList<Liberty>()
            for (neighbor in sameColorGroupNeighbors) {
                for (liberty in neighbor.liberties) {
                    if (!liberties.contains(liberty)) {
                        liberties.add(liberty)
                    }
                }
            }
            return liberties
        }

    // Stones

    val sameColorNeighbors: List<Stone>
        get() {
            val neighbors = ArrayList<Stone>(4)
            for (section in goban.getNeighbors(point)) {
                if (section is Stone) {
                    if (section.color == color) {
                        neighbors.add(section)
                    }
                }
            }
            return neighbors
        }

    // StoneGroups

    // TODO move: into stonegroup or goban
    val sameColorGroupNeighbors: List<StoneGroup>
        get() {
            val neighbors = ArrayList<StoneGroup>(4)
            for (section in goban.getNeighbors(point)) {
                if (section is Stone) {
                    if (section.color == color && !neighbors.contains(section.stoneGroup)) {
                        neighbors.add(section.stoneGroup!!)
                    }
                }
            }
            return neighbors
        }

    // TODO move: into stonegroup or goban
    val groupNeighbors: List<StoneGroup>
        get() {
            val neighbors = ArrayList<StoneGroup>(4) //TODO replace by a Set
            for (section in goban.getNeighbors(point)) {
                if (section is Stone) {
                    if (!neighbors.contains(section.stoneGroup)) {
                        neighbors.add(section.stoneGroup!!)
                    }
                }
            }
            return neighbors
        }

    // Indicateurs

    private val captureValue: Int
        get() {
            var value = 0
            for (neighbor in groupNeighbors) {
                if (neighbor.color == opponentColor && neighbor.liberties.size == 1) {
                    value += neighbor.stones.size
                }
            }
            return value
        }

    // TODO: Move into stonegroup
    val isPotentialKo: Boolean
        get() = stoneGroup!!.stones.size == 1 && liberties.size == 1

    val isMoveValid: Boolean
        get() {
            if (!goban.isLiberty(point)) {
                return false
            } else {
                return actualLiberties.size > 0 || captureValue > 0
            }
        }

    /**
     * Methode appellée par goban pour poser la pierre, aprés avoir checké que
     * le coup est légal
     */
    fun put() {
        capturedStones.clear()
        reput()
    }

    /**
     * Methode appellée par put ou undo, pour poser une pierre qui avait été
     * capturée par le passé.
     */
    private fun reput() {
        removeNeighborLiberty()
        stoneGroup = StoneGroup(this)
        goban.set(this)
        stoneGroup!!.merge(sameColorGroupNeighbors)
    }

    /**
     * Appellé par reput et merge Permet de supprimmer les libertées des groupes
     * enemis adjacent, et de capturer ceux dont les libertés == 0
     */
    private fun removeNeighborLiberty() {
        val liberty = goban.getLiberty(point)
        for (neighbor in groupNeighbors) {
            if (neighbor.color != color) {
                neighbor.remove(liberty)
                if (neighbor.liberties.size == 0) {
                    capturedStones.addAll(neighbor.stones)
                }
            }
        }
    }

    /**
     * Appellé par history. Annule le coup précédent
     */
    fun undo() {
        for (captured in capturedStones) {
            captured.reput()
        }
        split()
        stoneGroup = null
    }

    /**
     * Appellé par undo et stoneGroup.capture Rajoute des libertés au groups
     * adjacent du au retrait de la pierre
     */
    fun addNeighborLiberty() {
        val liberty = Liberty(Section.SColor.NONE, point, goban)
        for (neighbor in groupNeighbors) {
            neighbor.add(liberty)
        }
        goban.set(liberty)
    }

    /**
     * Appellé par undo. Divise les groupes aprés le retrait d'une pierre
     * faisant la liaison.
     */
    // TODO: move into stoneGroup
    private fun split() {
        for (neighbor in sameColorNeighbors) {
            if (neighbor.stoneGroup === stoneGroup) {
                neighbor.stoneGroup = StoneGroup(neighbor)
                neighbor.stoneGroup!!.rebuild()
            }
        }
    }
}
