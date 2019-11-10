package com.gmail.gcolaianni5.jris

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.io.*

/**
 * Convenience methods offering to directly work with IO methods.
 */
class JRisIO {

    companion object {

        //region:process -> RISFile lines -> RisRecords

        /**
         * Converts the RISFile lines provided by the reader into a list of [RisRecord]s.
         * May throw an [IOException] if the reader fails to deliver lines
         * or a [JRisException] if the lines cannot be parsed successfully.
         */
        @JvmStatic
        @ExperimentalCoroutinesApi
        @Throws(IOException::class)
        fun process(reader: Reader): List<RisRecord> = runBlocking(Dispatchers.IO) {
            BufferedReader(reader).readLines().asFlow().toRisRecords().toList()
        }

        /**
         * Converts the RISFile lines in the provided [File] into a list of [RisRecord]s.
         * May throw an [IOException] if the file cannot be read successfully.
         * or a [JRisException] if the lines cannot be parsed successfully.
         */
        @JvmStatic
        @ExperimentalCoroutinesApi
        @Throws(IOException::class)
        fun process(file: File): List<RisRecord> = process(file.bufferedReader())

        /**
         * Converts the RISFile lines from the file with the provided path into a list of [RisRecord]s.
         * May throw an [IOException] if the file cannot be read successfully.
         * or a [JRisException] if the lines cannot be parsed successfully.
         */
        @JvmStatic
        @ExperimentalCoroutinesApi
        @Throws(IOException::class)
        fun process(filePath: String): List<RisRecord> = process(File(filePath).bufferedReader())

        /**
         * Converts the RISFile lines provided by the [InputStream] into a list of [RisRecord]s.
         * May throw an [IOException] if the stream cannot be read successfully.
         * or a [JRisException] if the lines cannot be parsed successfully.
         */
        @JvmStatic
        @ExperimentalCoroutinesApi
        @Throws(IOException::class)
        fun process(inputStream: InputStream): List<RisRecord> = process(inputStream.bufferedReader())

        //endregion

        //region: build -> RisRecords -> RISFile lines

        /**
         * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, dumping them into the
         * provided [Writer]. Optionally accepts a list of names of [RisTag]s defining a sort order for
         * the [RisTag]s in the file.
         */
        @JvmStatic
        @JvmOverloads
        @ExperimentalCoroutinesApi
        fun build(records: List<RisRecord>, sort: List<String> = emptyList(), writer: Writer) {
            writer.use { w ->
                runBlocking(Dispatchers.IO) {
                    JRis.build(records.asFlow(), sort).toList().forEach { line ->
                        w.write(line)
                    }
                }
            }
        }

        /**
         * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, writing them into the provided [File].
         * Optionally accepts a list of names of [RisTag]s defining a sort order for the [RisTag]s in the file.
         */
        @JvmStatic
        @JvmOverloads
        @ExperimentalCoroutinesApi
        fun build(records: List<RisRecord>, sort: List<String> = emptyList(), file: File) {
            FileWriter(file).use { fileWriter ->
                build(records, sort, fileWriter)
            }
        }

        /**
         * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, writing them into
         * the provided [OutputStream]. Optionally accepts a list of names of [RisTag]s defining a sort order
         * for the [RisTag]s in the file.
         */
        @JvmStatic
        @JvmOverloads
        @ExperimentalCoroutinesApi
        fun build(records: List<RisRecord>, sort: List<String> = emptyList(), out: OutputStream) {
            OutputStreamWriter(out).use { writer ->
                build(records, sort, writer)
            }
        }

        /**
         * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, writing them into file with
         * the specified path if possible.
         * Optionally accepts a list of names of [RisTag]s defining a sort order for the [RisTag]s in the file.
         */
        @JvmStatic
        @JvmOverloads
        @ExperimentalCoroutinesApi
        fun build(records: List<RisRecord>, sort: List<String> = emptyList(), filePath: String) {
            FileOutputStream(filePath).use {
                build(records, sort, it)
            }
        }

        //endregion
    }
}

//region Kotlin extension functions for importing JRis files

/**
 * Converts the RISFile lines provided by the reader as receiver into a list of [RisRecord]s.
 * May throw an [IOException] if the reader fails to deliver lines or a [JRisException]
 * if the lines cannot be parsed successfully.
 */
fun Reader.process() = JRisIO.process(this)

/**
 * Converts the RISFile lines in the [File] provided as receiver into a list of [RisRecord]s.
 * May throw an [IOException] if the file cannot be read successfully.
 * or a [JRisException] if the lines cannot be parsed successfully.
 */
fun File.process() = JRisIO.process(this)

/**
 * Converts the RISFile lines from the file with the path provided as receiver into a list of [RisRecord]s.
 * May throw an [IOException] if the file cannot be read successfully.
 * or a [JRisException] if the lines cannot be parsed successfully.
 */
fun String.process() = JRisIO.process(this)

/**
 * Converts the RISFile lines provided by the [InputStream] as receiver  into a list of [RisRecord]s.
 * May throw an [IOException] if the stream cannot be read successfully.
 * or a [JRisException] if the lines cannot be parsed successfully.
 */
fun InputStream.process() = JRisIO.process(this)

//endregion

//region Kotlin extension functions for exporting JRis files

/**
 * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, dumping them into the
 * [Writer] provided as receiver. Optionally accepts a list of names of [RisTag]s defining a sort order for
 * the [RisTag]s in the file.
 */
fun Writer.build(records: List<RisRecord>, sort: List<String> = emptyList()) = JRisIO.build(records, sort, this)

/**
 * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, writing them into the [File]
 * provided as receiver. Optionally accepts a list of names of [RisTag]s defining a sort order for the [RisTag]s
 * in the file.
 */
fun File.build(records: List<RisRecord>, sort: List<String> = emptyList()) = JRisIO.build(records, sort, this)

/**
 * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, writing them into
 * the [OutputStream] provided as receiver. Optionally accepts a list of names of [RisTag]s defining a sort order
 * for the [RisTag]s in the file.
 */
fun OutputStream.build(records: List<RisRecord>, sort: List<String> = emptyList()) = JRisIO.build(records, sort, this)

/**
 * Converts a list of [RisRecord]s into a list of [String]s in RIS file format, writing them into file with
 * the path provided as the receiver, if possible.
 * Optionally accepts a list of names of [RisTag]s defining a sort order for the [RisTag]s in the file.
 */
fun String.build(records: List<RisRecord>, sort: List<String> = emptyList()) = JRisIO.build(records, sort, this)

//endregion
