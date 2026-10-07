package com.example.parser

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object PdfTextExtractor {
    private const val TAG = "PdfTextExtractor"
    private var isInitialized = false

    fun extractText(context: Context, uri: Uri): Result<String> {
        return try {
            if (!isInitialized) {
                PDFBoxResourceLoader.init(context.applicationContext)
                isInitialized = true
            }

            val contentResolver = context.contentResolver
            val stream: InputStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalArgumentException("Could not open input stream for URI"))

            val document = PDDocument.load(stream)
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            val text = stripper.getText(document)
            document.close()
            stream.close()

            if (text.isNotBlank()) {
                Result.success(text)
            } else {
                // Try fallback stream reading in case the PDF has simple text streams
                val fallbackText = extractRawTextFallback(context, uri)
                if (fallbackText.isNotBlank()) {
                    Result.success(fallbackText)
                } else {
                    Result.failure(IllegalStateException("No readable text could be extracted from this PDF."))
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "PDFBox extraction failed, attempting fallback", e)
            try {
                val fallbackText = extractRawTextFallback(context, uri)
                if (fallbackText.isNotBlank()) {
                    Result.success(fallbackText)
                } else {
                    Result.failure(e)
                }
            } catch (fallbackError: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun extractRawTextFallback(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.ISO_8859_1))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        val textTokenRegex = Regex("""\((.*?)\)\s*T[jJ]""")

        while (line != null) {
            val matches = textTokenRegex.findAll(line)
            for (match in matches) {
                val token = match.groupValues[1]
                if (token.isNotBlank()) {
                    sb.append(token).append(" ")
                }
            }
            if (line.contains("ET")) {
                sb.append("\n")
            }
            line = reader.readLine()
        }
        reader.close()
        inputStream.close()
        return sb.toString().trim()
    }
}
