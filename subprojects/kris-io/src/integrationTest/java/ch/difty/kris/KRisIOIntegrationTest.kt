package ch.difty.kris

import ch.difty.kris.domain.RisRecord
import ch.difty.kris.domain.RisType
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Test
import java.io.File
import java.util.stream.Collectors.toList

private const val FILE_PATH = "src/integrationTest/resources/sample.ris"
private const val PAPER_COUNT = 4

@Suppress("FunctionName", "S100", "SpellCheckingInspection", "MaxLineLength", "TooManyFunctions")
internal class KRisIOIntegrationTest {

    private val expectedLines = setOf(
        "Exposure to traffic noise and air pollution and risk for febrile seizure: a cohort study.",
        "Impact of Road Traffic Pollution on Pre-eclampsia and Pregnancy-induced Hypertensive Disorders.",
        "Exposure to long-term air pollution and road traffic noise in relation to cholesterol: A cross-sectional study.",
        "∂ for Data: Differentiating Data Structures",
    )

    //region Importing
    @Test
    fun `can read from reader`() {
        val parsed = File(FILE_PATH).bufferedReader().process()
        parsed shouldHaveSize PAPER_COUNT

        parsed.map { it.title } shouldContainAll expectedLines
    }

    @Test
    fun `can read from file`() {
        File(FILE_PATH).process() shouldHaveSize PAPER_COUNT
    }

    @Test
    fun `can read from file path`() {
        FILE_PATH.process() shouldHaveSize PAPER_COUNT
    }

    @Test
    fun `can read from file stream`() {
        File(FILE_PATH).inputStream().process() shouldHaveSize PAPER_COUNT
    }

    @Test
    fun `can read from reader to stream`() {
        val parsed = File(FILE_PATH).bufferedReader().processToStream().collect(toList())
        parsed shouldHaveSize PAPER_COUNT
        parsed.map { it.title } shouldContainAll expectedLines
    }

    @Test
    fun `can read from file to stream`() {
        File(FILE_PATH).processToStream().collect(toList()) shouldHaveSize PAPER_COUNT
    }

    @Test
    fun `can read from file path to stream`() {
        FILE_PATH.processToStream().collect(toList()) shouldHaveSize PAPER_COUNT
    }

    @Test
    fun `can read from file stream to stream`() {
        File(FILE_PATH).inputStream().processToStream().collect(toList()) shouldHaveSize PAPER_COUNT
    }

    //endregion

    //region Exporting

    private val records = listOf(
        RisRecord(type = RisType.JOUR, authors = mutableListOf("Author, A"), title = "Some title"),
        RisRecord(type = RisType.JOUR, authors = mutableListOf("Other-Author, B"), title = "Some other title"),
        RisRecord(type = RisType.JOUR, authors = mutableListOf("Third-Author, C"), title = "Some other title"),
    )

    @Test
    fun `can write to writer`() {
        val tmpFile = File.createTempFile("kris1", null, null)
        tmpFile.deleteOnExit()
        tmpFile.bufferedWriter().accept(records)
        tmpFile.process() shouldHaveSize records.size
    }

    @Test
    fun `can write to file`() {
        val tmpFile = File.createTempFile("kris2", null, null)
        tmpFile.deleteOnExit()
        tmpFile.accept(records)
        tmpFile.process() shouldHaveSize records.size
    }

    @Test
    fun `can write to stream`() {
        val tmpFile = File.createTempFile("kris3", null, null)
        tmpFile.deleteOnExit()
        tmpFile.outputStream().accept(records)
        tmpFile.process() shouldHaveSize records.size
    }

    @Test
    fun `can write to path`() {
        val tmpFile = File.createTempFile("kris4", null, null)
        val path = tmpFile.path
        tmpFile.delete()
        path.accept(records)
        tmpFile.process() shouldHaveSize records.size
        tmpFile.delete()
    }
    //endregion
}
