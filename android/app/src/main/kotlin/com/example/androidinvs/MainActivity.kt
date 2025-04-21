package com.example.androidinvs

import android.content.Intent
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : FlutterActivity() {
    private val channel = "com.example.androidinvs/crash_logs"
    private val tag = "MainActivity"
    private lateinit var methodChannel: MethodChannel

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        Log.d(tag, "Configuring Flutter engine")
        
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channel)

        // Set up uncaught exception handler for native crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(tag, "Uncaught exception in thread ${thread.name}", throwable)
            val crashLog = getCrashLog(throwable)
            try {
                methodChannel.invokeMethod("onCrash", crashLog)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send crash log to Flutter", e)
            }
            // Don't finish the activity, let Flutter handle the crash
        }

        methodChannel.setMethodCallHandler { call, result ->
            Log.d(tag, "Method call received: ${call.method}")
            when (call.method) {
                "getCrashLogs" -> {
                    val crashLogs = getCrashLogs()
                    result.success(crashLogs)
                }
                "getAdbLogs" -> {
                    val adbLogs = getAdbLogs()
                    result.success(adbLogs)
                }
                "openNativeActivity" -> {
                    try {
                        Log.d(tag, "Attempting to launch SecondActivity")
                        val intent = Intent(this, SecondActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Log.d(tag, "SecondActivity launched successfully")
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to launch SecondActivity: ${e.message}", e)
                        result.error("ERROR", "Failed to open native activity: ${e.message}", null)
                    }
                }
                else -> {
                    Log.w(tag, "Unknown method called: ${call.method}")
                    result.notImplemented()
                }
            }
        }
    }

    private fun getCrashLog(throwable: Throwable): String {
        val crashLog = StringBuilder()
        crashLog.append("Crash Time: ${System.currentTimeMillis()}\n")
        crashLog.append("Exception: ${throwable.javaClass.name}\n")
        crashLog.append("Message: ${throwable.message}\n")
        crashLog.append("Stack Trace:\n")
        throwable.stackTrace.forEach { element ->
            crashLog.append("\t${element}\n")
        }
        return crashLog.toString()
    }

    private fun getCrashLogs(): String {
        return "No crash logs available"
    }

    private fun getAdbLogs(): String {
        return try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val log = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                log.append(line).append("\n")
            }
            log.toString()
        } catch (e: IOException) {
            Log.e(tag, "Error getting ADB logs", e)
            "Error getting ADB logs: ${e.message}"
        }
    }
}

// Singleton object to store crash logs
object CrashLogger {
    private var lastCrashLog: String? = null

    fun logCrash(throwable: Throwable) {
        lastCrashLog = throwable.stackTraceToString()
    }

    fun getLastCrashLog(): String? {
        return lastCrashLog
    }
}