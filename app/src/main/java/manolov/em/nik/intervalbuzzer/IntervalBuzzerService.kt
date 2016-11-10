package manolov.em.nik.intervalbuzzer

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.os.Vibrator
import android.preference.PreferenceManager
import android.util.Log
import java.util.*

class IntervalBuzzerService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object{
        val TAG = "IntervalBuzzerService"
    }
    val vibrator: Vibrator? by lazy{getSystemService(Context.VIBRATOR_SERVICE) as Vibrator}
    val alarmManager: AlarmManager by lazy{applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager}
    val sharedPreferences: SharedPreferences by lazy{PreferenceManager.getDefaultSharedPreferences(this)}

    // OnSharedPreferenceChangeListener ---------------
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(TAG, "onSharedPreferenceChanged " + key)
        when (key) {
            "on_off", "interval" -> {
                val intent = Intent(applicationContext, IntervalBuzzerService::class.java)
                val pendingIntent = PendingIntent.getService(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                // reset current configuration
                alarmManager.cancel(pendingIntent)
                vibrator?.cancel()

                if (sharedPreferences?.getBoolean("on_off", false) ?: false) { //start
                    Log.d(TAG, "onSharedPreferenceChanged ON")
                    val calendar = Calendar.getInstance()
                    val repeatTime: Long = sharedPreferences?.getLong("interval", 60) ?: 60
                    calendar.add(Calendar.MINUTE, repeatTime.toInt()) // start with a *repeatTime* delay
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, repeatTime, pendingIntent)
                }
            }
            else -> return
        }
    }

    // Service override ----------------------
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate(){
        Log.d(TAG, "onCreate")
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        vibrate("10") //vibrator?.vibrate(10) ?: Log.d(TAG, "vibrator is not available")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int):Int{
        Log.d(TAG, "onStartCommand")
        vibrate(sharedPreferences.getString("buzz_interval", ""))
        return START_STICKY
    }

    override fun onDestroy(){
        Log.d(TAG, "onDestroy")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        vibrator?.cancel()
    }

    // this + ---------------------------------
    fun vibrate(pattern: String) {
        Log.d(TAG, "vibrate")
        vibrator?.cancel()
        var arrPattern = pattern.replace(" ", "").split(',').map{ it.toLong()}.toLongArray()
        arrPattern = longArrayOf(0).plus(arrPattern)
        if (arrPattern.count() > 0) {
            vibrator?.vibrate(arrPattern, -1) ?: Log.d(TAG, "vibrator is not available")
        }
    }
}
