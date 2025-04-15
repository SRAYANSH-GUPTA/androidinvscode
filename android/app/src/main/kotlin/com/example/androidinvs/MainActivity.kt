package com.example.androidinvs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.androidinvs/crash_logs"
    private val TAG = "MainActivity"

    // Thread to handle uncaught exceptions
    init {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception: ", throwable)
            // Store the crash information for later retrieval
            CrashLogger.logCrash(throwable)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getCrashLogs" -> {
                    result.success(getCrashLogs())
                }
                "getAdbLogs" -> {
                    result.success(getAdbLogs())
                }
                "openNativeActivity" -> {
                    openNativeActivity()
                    result.success(true)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun getCrashLogs(): String {
        return CrashLogger.getLastCrashLog() ?: "No crash logs available"
    }

    private fun getAdbLogs(): String {
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = BufferedReader(
                InputStreamReader(process.inputStream)
            )

            val log = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                log.append(line)
                log.append('\n')
            }

            return log.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ADB logs", e)
            return "Error getting ADB logs: ${e.message}"
        }
    }

    private fun openNativeActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
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