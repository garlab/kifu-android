package fr.narwhals.go.domain

import java.io.Serializable
import java.util.*

class Game(val size: Int, val handicap: Int, val rule: Game.Rule) : Serializable {
    enum class Rule internal constructor(val komi: Double) {
        Japanese(6.5), Chinese(7.5)
    }

    enum class Overtime {
        Byoyomi, Canadian, Absolute
    }

    var time: Int = 0
    var name: String? = null
    var dates: Array<Date> = arrayOf(Date())
    var copyright: String? = null
    var comment: String? = null
    var event: String? = null
    var round: String? = null
    var place: String? = null
    var source: String? = null
    var annotation: String? = null
    var user: String? = null

    val hoshis: List<Point>
    val handicaps: List<Point>

    val komi: Double
        get() = if (handicap == 0) 0.5 else rule.komi

    init {
        hoshis = getHoshis(size)
        handicaps = getHandicaps(handicap)
    }

	private fun getHoshis(size: Int): List<Point> {
        val hoshis = ArrayList<Point>(9)
		val pos = if (size == 9) 3 else 4
		val v = intArrayOf(pos, size / 2 + 1, size - pos + 1)

		for (i in 0..8) {
			hoshis += Point(v[i % 3], v[i / 3])
		}

        return hoshis
	}

    private fun getHandicaps(handicap: Int): List<Point> {
        val handicaps = ArrayList<Point>(handicap)
        if (handicap > 0) {
            handicaps += hoshis[0]
        }
        if (handicap > 1) {
            handicaps += hoshis[8]
        }
        if (handicap > 2) {
            handicaps += hoshis[2]
        }
        if (handicap > 3) {
            handicaps += hoshis[6]
        }
        if (handicap > 5) {
            handicaps += hoshis[1]
            handicaps += hoshis[7]
        }
        if (handicap > 7) {
            handicaps += hoshis[3]
            handicaps += hoshis[5]
        }
        if (handicap > 4 && handicap % 2 == 1) {
            handicaps += hoshis[4]
        }

        return handicaps
    }
}
