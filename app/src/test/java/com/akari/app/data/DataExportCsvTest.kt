package com.akari.app.data

import org.junit.Assert.assertTrue
import org.junit.Test

class DataExportCsvTest {
    private fun row(name: String) = EntryEntity(
        id = 1, epochDay = 20_000, kind = "activity", name = name, sub = null,
        epochMillis = 1_728_000_000_000L, timeLabel = "09:00",
        p = 0, c = 0, e = 0, total = 0,
    )

    @Test fun dangerousSpreadsheetPrefixesArePreservedAsText() {
        for (name in listOf("=1+1", "+SUM(1,2)", "-1+2", "@HYPERLINK", "  =1+1")) {
            val csv = DataExport.toCsv(emptyList(), listOf(row(name)))
            assertTrue("Unescaped formula prefix: $name", csv.contains("'$name"))
        }
    }

    @Test fun embeddedCommasAndQuotesRemainValidCsv() {
        val csv = DataExport.toCsv(emptyList(), listOf(row("Pain, \"then\" fatigue")))
        assertTrue(csv.contains("\"Pain, \"\"then\"\" fatigue\""))
    }

    @Test fun ordinaryNamesRemainUnchanged() {
        val csv = DataExport.toCsv(emptyList(), listOf(row("Morning walk")))
        assertTrue(csv.contains(",Morning walk,"))
    }
}
