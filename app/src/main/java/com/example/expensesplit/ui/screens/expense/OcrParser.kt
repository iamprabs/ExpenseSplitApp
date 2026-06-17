package com.example.expensesplit.ui.screens.expense

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object OcrParser {
    suspend fun parseReceipt(context: Context, uri: Uri): ParsedReceipt? = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val fullText = result.text
                    var parsedAmount: Double? = null
                    var parsedTitle: String? = null
                    
                    // Match decimal amounts: e.g. 10.50, 1,250.00
                    val amountRegex = Regex("""(?:\$|USD|INR|EUR)?\s*(\d{1,5}(?:[.,]\d{3})*[.,]\d{2})""")
                    val matches = amountRegex.findAll(fullText)
                    
                    val values = matches.mapNotNull { match ->
                        val cleaned = match.groupValues[1].replace(",", "")
                        cleaned.toDoubleOrNull()
                    }.toList()
                    
                    if (values.isNotEmpty()) {
                        // The max value is highly likely to be the total
                        parsedAmount = values.maxOrNull()
                    }
                    
                    val lines = fullText.split("\n").map { it.trim() }.filter { it.length > 2 }
                    val firstGoodLine = lines.firstOrNull { line ->
                        val hasNumbers = line.any { it.isDigit() }
                        !hasNumbers && !line.contains("$") && 
                        !line.contains("TOTAL", ignoreCase = true) && 
                        !line.contains("TAX", ignoreCase = true) &&
                        !line.contains("SUBTOTAL", ignoreCase = true)
                    }
                    if (firstGoodLine != null) {
                        parsedTitle = firstGoodLine
                    }
                    
                    if (parsedAmount != null || parsedTitle != null) {
                        continuation.resume(
                            ParsedReceipt(
                                title = parsedTitle ?: "Receipt Expense",
                                amount = parsedAmount ?: 0.0
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback helper if file reading failed
            continuation.resume(null)
        }
    }
}

data class ParsedReceipt(
    val title: String,
    val amount: Double
)
