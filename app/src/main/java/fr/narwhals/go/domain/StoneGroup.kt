package fr.narwhals.go.domain

import java.io.Serializable
import java.util.LinkedList

import fr.narwhals.go.domain.Section.SColor

data class StoneGroup(val stone: Stone) : Serializable {

    val territories = LinkedList<Territory>()
    val stones = LinkedList<Stone>()
    val liberties = LinkedList<Liberty>()

    var isDead = false
        private set

    init {
        add(stone)
    }

    val color: SColor
        get() = stones[0].color

    val numberOfLiberties: Int
        get() = liberties.size

    fun hasLiberty(liberty: Liberty): Boolean {
        return liberties.contains(liberty)
    }

    val numberOfStones: Int
        get() = stones.size

    fun getLibertiesAdded(stone: Stone): Int {
        var numberOfLibertiesAdded = -1
        for (liberty in stone.liberties) {
            if (!hasLiberty(liberty)) {
                numberOfLibertiesAdded++
            }
        }
        return numberOfLibertiesAdded
    }

    // Setter

    fun add(stone: Stone) {
        stones.add(stone)
        add(stone.liberties)
    }

    fun add(liberties: List<Liberty>) {
        for (liberty in liberties) {
            add(liberty)
        }
    }

    fun add(liberty: Liberty) {
        if (!liberties.contains(liberty)) {
            liberties.add(liberty)
        }
    }

    fun remove(liberty: Liberty) {
        liberties.remove(liberty)
        if (numberOfLiberties == 0) {
            capture()
            liberties.clear()
        }
    }

    private fun capture() {
        for (removed in stones) {
            removed.addNeighborLiberty()
        }
        this.isDead = true
    }

    fun rebuild() {
        addNeighbors(stones[0])
    }

    private fun addNeighbors(stone: Stone) {
        for (neighbor in stone.sameColorNeighbors) {
            if (neighbor.stoneGroup !== this) {
                neighbor.stoneGroup = this
                add(neighbor)
                addNeighbors(neighbor)
            }
        }
    }

    fun merge(merges: List<StoneGroup>) {
        for (merge in merges) {
            merge(merge)
        }
    }

    private fun merge(groupToMerge: StoneGroup) {
        for (stone in groupToMerge.stones) {
            add(stone)
            stone.stoneGroup = this
        }
    }

    // Territory

    fun mark() {
        val opponent = color.opponentColor
        isDead = !isDead
        for (territory in territories) {
            if (isDead) {
                if (territory.color != opponent) {
                    territory.color = opponent
                    territory.refresh()
                }
            } else {
                if (territory.color == opponent) {
                    territory.addColor(color)
                    territory.refresh()
                }
            }
        }
    }
}
