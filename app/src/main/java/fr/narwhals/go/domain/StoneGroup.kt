package fr.narwhals.go.domain

import java.io.Serializable
import java.util.LinkedList

import android.util.Log
import fr.narwhals.go.domain.Section.SColor

data class StoneGroup(val stone: Stone) : Serializable {

    private val territories: MutableList<Territory>
    private val stones: MutableList<Stone>
    private val liberties: MutableList<Liberty>
    var isDead = false
        private set

    init {
        this.territories = LinkedList<Territory>()
        this.stones = LinkedList<Stone>()
        this.liberties = LinkedList<Liberty>()
        add(stone)
    }

    /**
     * Appellé par stone Permet d'ajouter les pierres lors d'une capture
     */
    fun getStones(): List<Stone> {
        return stones
    }

    fun getLiberties(): List<Liberty> {
        return liberties
    }

    val color: SColor
        get() = stones[0].color!!

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

    fun remove(stone: Stone) {
        stones.remove(stone)
        refresh()
    }

    private fun refresh() {
        liberties.clear()
        for (stone in getStones()) {
            add(stone.liberties)
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
        for (removed in getStones()) {
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
        for (stone in groupToMerge.getStones()) {
            add(stone)
            stone.stoneGroup = this
        }
    }

    // Territory

    fun add(territory: Territory) {
        territories.add(territory)
    }

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
