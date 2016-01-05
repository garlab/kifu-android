package fr.narwhals.go.domain

import java.io.Serializable
import java.util.*

class GameInfo(val size: Int, val handicap: Int, val rule: GameInfo.Rule) : Serializable {
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

    var score: Float = 0f;

    val komi: Double
        get() = if (handicap == 0) 0.5 else rule.komi
}
