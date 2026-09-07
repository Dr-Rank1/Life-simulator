package com.maisha.game.data.model

/**
 * A first/last name pair produced by [com.maisha.game.data.remote.RandomNameService].
 */
data class GeneratedName(
    val first: String,
    val last: String,
    val gender: Gender
) {
    val fullName: String get() = "$first $last"
}
