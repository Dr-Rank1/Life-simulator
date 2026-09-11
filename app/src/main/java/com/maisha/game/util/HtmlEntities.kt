package com.maisha.game.util

/**
 * Decodes the HTML entities Open Trivia DB uses in questions and answers.
 * Pure Kotlin so domain/content code stays off Android APIs.
 */
object HtmlEntities {

    private val numeric = Regex("&#(\\d+);")
    private val hex = Regex("&#x([0-9a-fA-F]+);")

    private val named = mapOf(
        "&quot;" to "\"",
        "&apos;" to "'",
        "&#039;" to "'",
        "&amp;" to "&",
        "&lt;" to "<",
        "&gt;" to ">",
        "&nbsp;" to " ",
        "&eacute;" to "é",
        "&Eacute;" to "É",
        "&oacute;" to "ó",
        "&aacute;" to "á",
        "&iacute;" to "í",
        "&uacute;" to "ú",
        "&ntilde;" to "ñ",
        "&uuml;" to "ü",
        "&ouml;" to "ö",
        "&auml;" to "ä",
        "&rsquo;" to "'",
        "&lsquo;" to "'",
        "&rdquo;" to "\"",
        "&ldquo;" to "\"",
        "&hellip;" to "..."
    )

    fun decode(raw: String): String {
        var text = raw
        named.forEach { (entity, value) -> text = text.replace(entity, value) }
        text = hex.replace(text) { match ->
            val code = match.groupValues[1].toIntOrNull(16) ?: return@replace match.value
            code.toChar().toString()
        }
        text = numeric.replace(text) { match ->
            val code = match.groupValues[1].toIntOrNull() ?: return@replace match.value
            if (code in 1..0xFFFF) code.toChar().toString() else match.value
        }
        return text
    }
}
