package com.prabs.ceipts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.prabs.ceipts.ui.navigation.AppNavigation
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detectDefaultCurrency()
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }

    private fun detectDefaultCurrency() {
        val prefs = getSharedPreferences("ceipts_user_prefs", MODE_PRIVATE)
        if (prefs.contains("default_currency")) {
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            var currencyDetected = "USD"
            try {
                // Try ipapi.co first
                val url = URL("https://ipapi.co/json/")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val currency = json.optString("currency", "")
                    if (currency.isNotBlank()) {
                        currencyDetected = currency.uppercase()
                    }
                } else {
                    // Try fallback ip-api.com
                    val fallbackUrl = URL("http://ip-api.com/json")
                    val fallbackConn = fallbackUrl.openConnection() as HttpURLConnection
                    fallbackConn.requestMethod = "GET"
                    fallbackConn.connectTimeout = 5000
                    fallbackConn.readTimeout = 5000
                    if (fallbackConn.responseCode == HttpURLConnection.HTTP_OK) {
                        val responseText = fallbackConn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(responseText)
                        val countryCode = json.optString("countryCode", "US").uppercase()
                        currencyDetected = mapCountryToCurrency(countryCode)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Try fallback ip-api.com directly if first threw exception
                try {
                    val fallbackUrl = URL("http://ip-api.com/json")
                    val fallbackConn = fallbackUrl.openConnection() as HttpURLConnection
                    fallbackConn.requestMethod = "GET"
                    fallbackConn.connectTimeout = 5000
                    fallbackConn.readTimeout = 5000
                    if (fallbackConn.responseCode == HttpURLConnection.HTTP_OK) {
                        val responseText = fallbackConn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(responseText)
                        val countryCode = json.optString("countryCode", "US").uppercase()
                        currencyDetected = mapCountryToCurrency(countryCode)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            prefs.edit().putString("default_currency", currencyDetected).apply()
        }
    }

    private fun mapCountryToCurrency(countryCode: String): String {
        return when (countryCode) {
            "IN" -> "INR"
            "GB" -> "GBP"
            "JP" -> "JPY"
            "CA" -> "CAD"
            "AU" -> "AUD"
            "CH" -> "CHF"
            "CN" -> "CNY"
            "SG" -> "SGD"
            "DE", "FR", "IT", "ES", "NL", "BE", "AT", "FI", "GR", "IE", "PT" -> "EUR"
            else -> "USD"
        }
    }
}
