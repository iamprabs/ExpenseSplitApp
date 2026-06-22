package com.prabs.ceipts.ui.screens.expense

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

object OcrParser {

    suspend fun parseReceipt(context: Context, uri: Uri): ParsedReceipt? {
        // Try NVIDIA Multimodal API first
        val nvidiaResult = parseReceiptWithNvidia(context, uri)
        if (nvidiaResult != null) {
            Log.d("OcrParser", "Successfully parsed receipt via NVIDIA API: $nvidiaResult")
            return nvidiaResult
        }
        
        Log.d("OcrParser", "NVIDIA API failed, falling back to local ML Kit OCR")
        // Fallback to local ML Kit
        return parseReceiptWithMlKit(context, uri)
    }

    private fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null
            
            // Resize bitmap to max 1024px dimension
            val maxDimension = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height
            val newBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toDouble() / height.toDouble()
                val newWidth: Int
                val newHeight: Int
                if (ratio > 1.0) {
                    newWidth = maxDimension
                    newHeight = (maxDimension / ratio).toInt()
                } else {
                    newHeight = maxDimension
                    newWidth = (maxDimension * ratio).toInt()
                }
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }
            
            val outputStream = java.io.ByteArrayOutputStream()
            newBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            
            // Recycle bitmaps
            if (newBitmap != originalBitmap) {
                newBitmap.recycle()
            }
            originalBitmap.recycle()
            
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun parseReceiptWithNvidia(context: Context, uri: Uri): ParsedReceipt? = withContext(Dispatchers.IO) {
        try {
            val base64Image = uriToBase64(context, uri) ?: return@withContext null
            
            val apiKey = "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"
            val url = URL("https://integrate.api.nvidia.com/v1/chat/completions")
            
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            
            // Build payload
            val payload = JSONObject()
            payload.put("model", "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning")
            
            val messages = JSONArray()
            val message = JSONObject()
            message.put("role", "user")
            
            val content = JSONArray()
            
            val textObj = JSONObject()
            textObj.put("type", "text")
            textObj.put("text", "Extract the merchant name/title, total amount, and the individual line items from this receipt image. Format your response strictly as JSON: {\"title\": \"Merchant Name\", \"amount\": 12.34, \"items\": [{\"name\": \"Item Name\", \"price\": 12.34}]}. Do not include markdown code blocks (```json) or any explanations, return ONLY the raw JSON string.")
            content.put(textObj)
            
            val imageObj = JSONObject()
            imageObj.put("type", "image_url")
            val imageUrlDetails = JSONObject()
            imageUrlDetails.put("url", "data:image/jpeg;base64,$base64Image")
            imageObj.put("image_url", imageUrlDetails)
            content.put(imageObj)
            
            message.put("content", content)
            messages.put(message)
            payload.put("messages", messages)
            
            payload.put("temperature", 0.6)
            payload.put("top_p", 0.95)
            payload.put("max_tokens", 65536)
            payload.put("reasoning_budget", 16384)
            val chatTemplateKwargs = JSONObject()
            chatTemplateKwargs.put("enable_thinking", true)
            payload.put("chat_template_kwargs", chatTemplateKwargs)
            
            // Write payload
            conn.outputStream.use { os ->
                val input = payload.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }
            
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)
                val choices = responseJson.getJSONArray("choices")
                if (choices.length() > 0) {
                    val choice = choices.getJSONObject(0)
                    val messageObj = choice.getJSONObject("message")
                    val textContent = messageObj.getString("content")
                    
                    var cleanJson = textContent.trim()
                    val jsonStart = cleanJson.indexOf('{')
                    val jsonEnd = cleanJson.lastIndexOf('}')
                    if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                        cleanJson = cleanJson.substring(jsonStart, jsonEnd + 1)
                    } else if (cleanJson.startsWith("```")) {
                        cleanJson = cleanJson.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    }
                    
                    val parsedJson = JSONObject(cleanJson)
                    val title = parsedJson.optString("title", "Receipt Expense")
                    val amount = parsedJson.optDouble("amount", 0.0)
                    
                    val itemsArray = parsedJson.optJSONArray("items")
                    val items = mutableListOf<ParsedReceiptItem>()
                    if (itemsArray != null) {
                        for (i in 0 until itemsArray.length()) {
                            val itemObj = itemsArray.getJSONObject(i)
                            val itemName = itemObj.optString("name", "Item ${i + 1}")
                            val itemPrice = itemObj.optDouble("price", 0.0)
                            items.add(ParsedReceiptItem(itemName, itemPrice))
                        }
                    }
                    
                    ParsedReceipt(title = title, amount = amount, items = items)
                } else {
                    null
                }
            } else {
                val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("OcrParser", "NVIDIA API Error: $responseCode - $errorText")
                null
            }
        } catch (e: Exception) {
            Log.e("OcrParser", "NVIDIA API Exception", e)
            null
        }
    }

    private suspend fun parseReceiptWithMlKit(context: Context, uri: Uri): ParsedReceipt? = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val fullText = result.text
                    var parsedAmount: Double? = null
                    var parsedTitle: String? = null
                    
                    val amountRegex = Regex("""(?:\$|USD|INR|EUR)?\s*(\d{1,5}(?:[.,]\d{3})*[.,]\d{2})""")
                    val matches = amountRegex.findAll(fullText)
                    
                    val values = matches.mapNotNull { match ->
                        val cleaned = match.groupValues[1].replace(",", "")
                        cleaned.toDoubleOrNull()
                    }.toList()
                    
                    if (values.isNotEmpty()) {
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
                    
                    // Parse line items
                    val parsedItems = mutableListOf<ParsedReceiptItem>()
                    for (line in lines) {
                        val priceMatch = Regex("""(?:\$|USD|INR|EUR)?\s*(\d+(?:\.\d{2})?)\s*$""").find(line)
                        if (priceMatch != null) {
                            val priceVal = priceMatch.groupValues[1].toDoubleOrNull()
                            if (priceVal != null && priceVal > 0.0) {
                                val name = line.substring(0, priceMatch.range.first).trim().trimEnd { it == '$' || it == ' ' || it == '-' }
                                if (name.isNotEmpty() && 
                                    !name.contains("TOTAL", ignoreCase = true) && 
                                    !name.contains("TAX", ignoreCase = true) && 
                                    !name.contains("SUBTOTAL", ignoreCase = true)) {
                                    parsedItems.add(ParsedReceiptItem(name, priceVal))
                                }
                            }
                        }
                    }

                    if (parsedItems.isEmpty() && (parsedAmount != null || parsedTitle != null)) {
                        parsedItems.add(ParsedReceiptItem(parsedTitle ?: "Receipt Expense", parsedAmount ?: 0.0))
                    }
                    
                    if (parsedAmount != null || parsedTitle != null || parsedItems.isNotEmpty()) {
                        continuation.resume(
                            ParsedReceipt(
                                title = parsedTitle ?: "Receipt Expense",
                                amount = parsedAmount ?: 0.0,
                                items = parsedItems
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
            continuation.resume(null)
        }
    }
}

data class ParsedReceiptItem(
    val name: String,
    val price: Double
)

data class ParsedReceipt(
    val title: String,
    val amount: Double,
    val items: List<ParsedReceiptItem> = emptyList()
)

