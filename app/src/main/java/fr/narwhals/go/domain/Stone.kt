package fr.narwhals.go.domain

import java.util.ArrayList
import java.util.LinkedList

class Stone(color: Section.SColor, point: Point, goban: Goban?) : Section(color, point, goban) {

    /**
     * Utilisé par StoneGroup lors des merge
     */
    var stoneGroup: StoneGroup? = null
    val capturedStones = LinkedList<Stone>()

    /**
     * Called by History, allow to number the moves
     */
    var round = -1

    // Liberties

    /**
     * Return the list of liberties adjacent to the stone.
     */
    val liberties: MutableList<Liberty>
        get() {
            val liberties = ArrayList<Liberty>(4)
            for (neighbor in goban!!.getNeighbors(point)) {
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
                for (liberty in neighbor.getLiberties()) {
                    if (!liberties.contains(liberty)) {
                        liberties.add(liberty)
                    }
                }
            }
            liberties.remove(goban!!.getLiberty(point))
            return liberties
        }

    /**
     * Return unique liberties for each adjacent group.
     */
    val actualNeighborLiberties: List<Liberty>
        get() {
            val liberties = LinkedList<Liberty>()
            for (neighbor in sameColorGroupNeighbors) {
                for (liberty in neighbor.getLiberties()) {
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
            for (section in goban!!.getNeighbors(point)) {
                if (section is Stone) {
                    if (section.color == color) {
                        neighbors.add(section)
                    }
                }
            }
            return neighbors
        }

    // StoneGroups

    val sameColorGroupNeighbors: List<StoneGroup>
        get() {
            val neighbors = ArrayList<StoneGroup>(4)
            for (section in goban!!.getNeighbors(point)) {
                if (section is Stone) {
                    if (section.color == color && !neighbors.contains(section.stoneGroup)) {
                        neighbors.add(section.stoneGroup!!)
                    }
                }
            }
            return neighbors
        }

    val groupNeighbors: List<StoneGroup>
        get() {
            val neighbors = ArrayList<StoneGroup>(4)
            for (section in goban!!.getNeighbors(point)) {
                if (section is Stone) {
                    if (!neighbors.contains(section.stoneGroup)) {
                        neighbors.add(section.stoneGroup!!)
                    }
                }
            }
            return neighbors
        }

    // Indicateurs

    val captureValue: Int
        get() {
            var value = 0
            for (neighbor in groupNeighbors) {
                if (neighbor.color == opponentColor && neighbor.numberOfLiberties == 1) {
                    value += neighbor.numberOfStones
                }
            }
            return value
        }

    val numberOfLiberties: Int
        get() = liberties.size

    val actualNumberOfLiberties: Int
        get() = actualLiberties.size

    val actualNumberOfNeighborLiberties: Int
        get() = actualNeighborLiberties.size

    val isPotentialKo: Boolean
        get() = stoneGroup!!.numberOfStones == 1 && numberOfLiberties == 1

    val isMoveValid: Boolean
        get() {
            if (!goban!!.isLiberty(point)) {
                return false
            } else {
                return actualNumberOfLiberties > 0 || captureValue > 0
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
    fun reput() {
        removeNeighborLiberty()
        stoneGroup = StoneGroup(this)
        add()
        stoneGroup!!.merge(sameColorGroupNeighbors)
    }

    /**
     * Appellé par reput et merge Permet de supprimmer les libertées des groupes
     * enemis adjacent, et de capturer ceux dont les libertés == 0
     */
    fun removeNeighborLiberty() {
        val liberty = goban!!.getLiberty(point)
        for (neighbor in groupNeighbors) {
            if (neighbor.color != color) {
                neighbor.remove(liberty)
                if (neighbor.numberOfLiberties == 0) {
                    capturedStones.addAll(neighbor.getStones())
                }
            }
        }
    }

    /**
     * Appellé par history. Annule le coup précédent
     */
    fun undo() {
        addNeighborLiberty()
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
        val liberty = Liberty(Section.SColor.NONE, point, goban!!)
        for (neighbor in groupNeighbors) {
            neighbor.add(liberty)
        }
        liberty.add()
    }

    /**
     * Appellé par undo. Divise les groupes aprés le retrait d'une pierre
     * faisant la liaison.
     */
    private fun split() {
        for (neighbor in sameColorNeighbors) {
            if (neighbor.stoneGroup === stoneGroup) {
                neighbor.stoneGroup = StoneGroup(neighbor)
                neighbor.stoneGroup!!.rebuild()
            }
        }
    }

    companion object {
        val PASS = Stone(Section.SColor.NONE, Point(-1, -1), null)
    }
}
