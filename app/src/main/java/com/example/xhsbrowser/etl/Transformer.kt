package com.example.xhsbrowser.etl

import com.example.xhsbrowser.util.DateUtils
import java.util.regex.Pattern

class Transformer {

    private val seenHashes = HashSet<Int>()

    fun transform(rawRecords: List<RawRecord>): List<CleanRecord> {
        return rawRecords
            .mapNotNull { clean(it) }
            .filter { deduplicate(it) }
    }

    fun resetDeduplication() {
        seenHashes.clear()
    }

    private fun clean(raw: RawRecord): CleanRecord? {
        val title = cleanTitle(raw.title) ?: return null
        val author = cleanAuthor(raw.author)
        val snippet = cleanSnippet(raw.contentSnippet)
        val browseDate = DateUtils.formatDate(raw.browseTime)

        return CleanRecord(title, author, snippet, raw.browseTime, browseDate)
    }

    private fun cleanTitle(text: String): String? {
        var cleaned = text
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[\\uD800-\\uDFFF]"), "") // remove surrogate pairs (some emoji)
            .trim()

        if (cleaned.length < 2 || cleaned.length > 100) return null
        if (cleaned.matches(Regex("^[\\d,.，。、\\s]+$"))) return null // pure numbers/punctuation
        if (cleaned.contains("广告") || cleaned.contains("推广")) return null

        return cleaned
    }

    private fun cleanAuthor(text: String): String {
        return text
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[\\uD800-\\uDFFF]"), "")
            .trim()
            .take(30)
    }

    private fun cleanSnippet(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[\\uD800-\\uDFFF]"), "")
            .trim()
            .take(300)
    }

    private fun deduplicate(record: CleanRecord): Boolean {
        val hash = record.title.hashCode() * 31 + record.author.hashCode()
        if (seenHashes.contains(hash)) return false
        seenHashes.add(hash)
        return true
    }
}
