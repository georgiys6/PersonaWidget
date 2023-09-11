package dev.georgiys.personawidget

import android.content.Context
import androidx.preference.PreferenceManager
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException


fun getWeather(context: Context){
    val client = OkHttpClient()
    val preference = PreferenceManager.getDefaultSharedPreferences(context)
    val lat = preference.getString("latitude", "-200.0")
    val lon = preference.getString("longitude", "-200.0")
    val request: Request = Request.Builder()
        //TODO you can get your api from https://openweathermap.org/api
        .url("https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&APPID=${BuildConfig.UrlKey}")
        .build()

    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException(
                    "Request was fail: " +
                            response.code + " " + response.message
                )
            }
            val jsonObject = JSONObject(response.body!!.string())
            val weather = JSONObject(jsonObject.getJSONArray("weather").get(0).toString())
                .getString("description")
            preference.edit().putString("description",weather).apply()
            Log.d("getJson", weather)
            Log.d("checkConnect", "Server: " + response.header("Server"))
        }
    } catch (e: IOException) {
        Log.e("checkConnect", "Fail connection: $e")
    }
}