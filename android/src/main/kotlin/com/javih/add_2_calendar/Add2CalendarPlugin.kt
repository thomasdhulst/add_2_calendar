package com.javih.add_2_calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/** Add2CalendarPlugin */
class Add2CalendarPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var context: Context? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "add_2_calendar")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "add2Cal") {
                val success = insert(call.argument("title")!!,
                        call.argument("desc")!!,
                        call.argument("location")!!,
                        call.argument("startDate")!!,
                        call.argument("endDate")!!,
                        call.argument("timeZone") as String?,
                        call.argument("allDay")!!,
                        call.argument("recurrence") as HashMap<String,Any>?,
                        call.argument("invites") as String?,
                        call.argument("availability")!!
                )
                result.success(success)

        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    private fun insert(title: String, desc:String,  loc:String,  start:Long,  end:Long,  timeZone:String?,  allDay:Boolean,  recurrence:HashMap<String,Any>?,  invites:String?, availability: Int): Boolean {

        val mContext: Context = if (activity != null) activity!!.applicationContext else context!!
        val intent = Intent(Intent.ACTION_INSERT)


        intent.data = CalendarContract.Events.CONTENT_URI
        intent.putExtra(CalendarContract.Events.TITLE, title)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, desc)
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, loc)
        intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
        intent.putExtra(CalendarContract.Events.EVENT_END_TIMEZONE, timeZone)
        intent.putExtra(CalendarContract.Events.AVAILABILITY, availability)
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay)
        
        if (recurrence != null) {
            intent.putExtra(CalendarContract.Events.RRULE, buildRRule(recurrence))
        }

        if (invites != null) {
            intent.putExtra(Intent.EXTRA_EMAIL, invites)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if(intent.resolveActivity(mContext.packageManager)!= null){
            mContext.startActivity(intent)
            return true
        }
        return false;
    }


    private fun buildRRule(recurrence: HashMap<String,Any>): String? {

        var rRule = recurrence["rRule"] as String?
        if (rRule == null) {
            rRule = ""
            val freqEnum: Int? = recurrence["frequency"] as Int?
            if (freqEnum != null) {
                rRule += "FREQ="
                when (freqEnum) {
                    0 -> rRule += "DAILY"
                    1 -> rRule += "WEEKLY"
                    2 -> rRule += "MONTHLY"
                    3 -> rRule += "YEARLY"
                }
                rRule += ";"
            }
            rRule += "INTERVAL=" + recurrence["interval"] as Int + ";"
            val occurrences: Int? = recurrence["ocurrences"] as Int?
            if (occurrences != null) {
                rRule += "COUNT=" + occurrences.toInt().toString() + ";"
            }
            val endDateMillis = recurrence["endDate"] as Long?
            if (endDateMillis != null) {
                val endDate = Date(endDateMillis)
                val formatter: DateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss")
                rRule += "UNTIL=" + formatter.format(endDate).toString() + ";"
            }
        }
        return rRule
    }

}
