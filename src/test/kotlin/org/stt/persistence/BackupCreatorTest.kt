package org.stt.persistence

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFileFilter
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.MockitoAnnotations
import org.stt.config.ConfigRoot
import org.stt.config.PathSetting
import org.stt.time.DateTimes
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDate

class BackupCreatorTest {

    @field:Rule
    @JvmField
    var tempFolder = TemporaryFolder()

    private val configRoot = ConfigRoot()
    private val backupConfig = configRoot.backup

    private var currentTempFolder: File? = null

    private lateinit var currentSttFile: File
    private var sut: BackupCreator? = null

    @Before
    @Throws(IOException::class)
    fun setup() {
        MockitoAnnotations.initMocks(this)

        currentTempFolder = tempFolder.newFolder()
        currentSttFile = tempFolder.newFile()
        PrintWriter(currentSttFile, StandardCharsets.UTF_8.name()).use { out -> out.print("blubb, just a test line") }

        backupConfig.backupRetentionCount = 3
        backupConfig.backupLocation = PathSetting(currentTempFolder!!.absolutePath)

        sut = BackupCreator(backupConfig, currentSttFile, "")
    }

    @Test
    @Throws(IOException::class)
    fun existingBackupShouldPreventNewBackup() {
        // GIVEN
        val threeDaysAgo = DateTimes.prettyPrintDate(LocalDate.now()
                .minusDays(3))
        val sttFileName = currentSttFile.name
        val backedUp = File(currentTempFolder, sttFileName + "-"
                + threeDaysAgo)
        createNewFile(backedUp)

        // WHEN
        sut!!.start()

        // THEN
        val files = FileUtils.listFiles(currentTempFolder!!,
                FileFileFilter.FILE, null)

        Assert.assertEquals(1, files.size.toLong())
        Assert.assertThat(files.iterator().next().absoluteFile,
                `is`(backedUp.absoluteFile))
    }

    @Test
    @Throws(IOException::class)
    fun oldBackupShouldBeDeleted() {
        // GIVEN
        for (i in 0 until backupConfig.backupRetentionCount) {
            val xDaysAgo = DateTimes.prettyPrintDate(LocalDate.now()
                    .minusDays(i.toLong()))
            val sttFileName = currentSttFile.name
            val oldFile = File(currentTempFolder, sttFileName + "-"
                    + xDaysAgo)
            createNewFile(oldFile)
        }

        val xDaysAgo = DateTimes.prettyPrintDate(LocalDate.now()
                .minusDays((backupConfig.backupRetentionCount + 1).toLong()))
        val sttFileName = currentSttFile.name
        val oldFile = File(currentTempFolder, "$sttFileName-$xDaysAgo")
        createNewFile(oldFile)

        // WHEN
        sut!!.start()

        // THEN
        Assert.assertFalse("Old backup file should have been deleted",
                oldFile.exists())
    }

    @Test
    @Throws(IOException::class)
    fun initialBackupShouldBeCreated() {
        // GIVEN
        val currentDate = DateTimes.prettyPrintDate(LocalDate.now())
        val sttFileName = currentSttFile.name
        val expectedFile = File(currentTempFolder, sttFileName + "-"
                + currentDate)

        // WHEN
        sut!!.start()

        // THEN
        Assert.assertTrue(
                "Original and backed up files do not have the same contents",
                FileUtils.contentEquals(currentSttFile, expectedFile))
    }

    @Test
    @Throws(IOException::class)
    fun existingFileShouldNotBeOverwritten() {
        // GIVEN
        val currentDate = DateTimes.prettyPrintDate(LocalDate.now())
        val sttFileName = currentSttFile.name
        val existingFile = File(currentTempFolder, sttFileName + "-"
                + currentDate)
        createNewFile(existingFile)

        // WHEN
        sut!!.start()

        // THEN
        Assert.assertFalse(
                "Original and backed up files do not have the same contents",
                FileUtils.contentEquals(currentSttFile, existingFile))
    }

    @Throws(IOException::class)
    private fun createNewFile(toCreate: File) {
        Assert.assertTrue(
                "could not create test file " + toCreate.absolutePath,
                toCreate.createNewFile())
    }
}
