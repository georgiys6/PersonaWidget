package dev.georgiys.personawidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.text.format.DateFormat
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import dev.georgiys.personawidget.R.drawable.cloudy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class WidgetPerson4 : AppWidgetProvider() {

    private var scope = CoroutineScope(Dispatchers.Main)
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {

        appWidgetIds?.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity.
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                /* context = */ context,
                /* requestCode = */  0,
                /* intent = */ intent,
                /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // Get the layout for the widget and attach an on-click listener
            // to the button.
            val alarm = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(pendingIntent)
            val interval = (1000).toLong()
            alarm.setRepeating(
                AlarmManager.RTC,
                SystemClock.elapsedRealtime(),
                interval,
                pendingIntent
            )
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_persona_4
            ).apply {
                setOnClickPendingIntent(R.id.mouth_persona_4, pendingIntent)
                setImageViewBitmap(R.id.mouth_persona_4, textAsBitmap(getDate(), context))
            }

            // Tell the AppWidgetManager to perform an update on the current
            // widget.
            appWidgetManager?.updateAppWidget(appWidgetId, views)
        }
    }

    private fun getPeriodOfDay(): String{
        val date = Calendar.getInstance()
        return when (date.get(Calendar.HOUR_OF_DAY)){
            in 0.. 5 -> "Night"
            in 6.. 8 -> "Early Morning"
            in 9.. 10 -> "Morning"
            in 11.. 13 -> "Afternoon"
            in 14 .. 16 -> "Daytime"
            in 17.. 23 -> "Evening"
            else -> "None"
        }
    }

    private fun getDayOfWeek(): String{
        val date = Date()
        val dateFormat = SimpleDateFormat("EE", Locale.ENGLISH).format(date)
        return dateFormat.toString().uppercase()
    }

    private fun getDate(): String{
        val date = Calendar.getInstance()
        val dateFormat = DateFormat.format("MM/dd", date)
        return dateFormat.toString()
    }

    private fun getIconWeather(context: Context): Int{
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        Log.w("getPreference",preference.getString("description", "few clouds")!!)
        return when (preference.getString("description", "few clouds")) {
            "shower rain", "rain" -> R.drawable.rain
            "clear sky" -> R.drawable.sunny
            "thunderstorm" -> R.drawable.storm
            "snow" -> R.drawable.snow
            else -> cloudy
        }
    }

    private fun textAsBitmap(text: String?, context: Context): Bitmap? {
        val paintDate = Paint()
        paintDate.textAlign = Paint.Align.LEFT
        paintDate.textSize = 148f
        paintDate.color = Color.WHITE
        paintDate.typeface = context.resources.getFont(R.font.hussar_bd_wide)
        paintDate.style = Paint.Style.FILL

        val paintDayWeek = Paint()
        paintDayWeek.textAlign = Paint.Align.LEFT
        paintDayWeek.textSize = 130f
        paintDayWeek.color = Color.WHITE
        paintDayWeek.typeface = context.resources.getFont(R.font.tempest)
        paintDayWeek.style = Paint.Style.FILL

        val paintPeriodOfDay = Paint()
        paintPeriodOfDay.textAlign = Paint.Align.LEFT
        paintPeriodOfDay.textSize = 150f
        paintPeriodOfDay.color = Color.WHITE
        paintPeriodOfDay.style = Paint.Style.FILL
        paintPeriodOfDay.typeface = context.resources.getFont(R.font.liberation_sans_regular)
        paintPeriodOfDay.setShadowLayer(10F, 0f,0f,Color.BLACK)

        val paintGray = Paint()
        paintGray.isAntiAlias = true
        paintGray.color = context.resources.getColor(R.color.gray, null)
        paintGray.style = Paint.Style.FILL

        val paintYellow = Paint()
        paintYellow.color = context.resources.getColor(R.color.yellow, null)
        paintYellow.style = Paint.Style.STROKE
        paintYellow.strokeWidth = 150f

        val paintOrange = Paint()
        paintOrange.isAntiAlias = true
        paintOrange.color = context.resources.getColor(R.color.orange, null)
        paintOrange.style = Paint.Style.STROKE
        paintOrange.strokeWidth = 20f

        val paintWhite = Paint()
        paintWhite.isAntiAlias = true
        paintWhite.color = context.resources.getColor(R.color.white, null)
        paintWhite.style = Paint.Style.STROKE
        paintWhite.strokeWidth = 15f

        val paintRed = Paint()
        paintRed.isAntiAlias = true
        paintRed.color = context.resources.getColor(R.color.red,null)
        paintRed.style = Paint.Style.STROKE
        paintRed.strokeWidth = 15f

        val baseline: Float = -paintDate.ascent() // ascent() is negative
        val width = (paintDate.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + 5.5f).toInt()
        val image = Bitmap.createBitmap(width + height + 400, height + 700, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(image)
        scope.launch { withContext(Dispatchers.IO){ getWeather(context) } }

        val imageRes = ResourcesCompat.getDrawable(context.resources, getIconWeather(context), null)?.toBitmap()
        canvas.drawCircle(375f + 100f,420f,330f, paintYellow)
        canvas.drawCircle(375f + 100f,420f,350f, paintOrange)
        canvas.drawCircle(375f + 100f,420f,330f, paintWhite)
        canvas.drawCircle(375f + 100f,420f,315f, paintRed)
        canvas.drawCircle(175f,600f,25f, paintYellow)
        canvas.drawCircle(175f,600f,85f, paintGray)
        canvas.drawCircle(height.toFloat()/2+ 75f, height.toFloat()/2 + 350F, height/2f, paintGray)
        canvas.drawRect(height/2f + 75f,350F,880F + 75f,height.toFloat() + 350F, paintGray)
        canvas.drawCircle(880F + 75f, height.toFloat()/2 + 350F, height/2f, paintGray)
        canvas.drawText(text ?: "", height.toFloat()/2 + 75f, baseline  + 350F, paintDate)
        canvas.drawText(getDayOfWeek(), height.toFloat()/2 + 125f + width, baseline  + 320F, paintDayWeek)
        canvas.drawText(getPeriodOfDay(), height.toFloat()/2 - 175f + width, baseline  + 520F, paintPeriodOfDay)
        canvas.drawBitmap(imageRes!!, 98f,523f,paintGray)
        return image
    }
}