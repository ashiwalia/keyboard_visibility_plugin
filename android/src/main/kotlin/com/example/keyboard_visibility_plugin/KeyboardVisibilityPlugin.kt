package com.example.keyboard_visibility_plugin


import android.app.Activity
import android.app.Application
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink

/** KeyboardVisibilityPlugin */
class KeyboardVisibilityPlugin : FlutterPlugin, EventChannel.StreamHandler,
    Application.ActivityLifecycleCallbacks,
    ViewTreeObserver.OnGlobalLayoutListener, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private val STREAM_CHANNEL_NAME = "github.com/adee42/flutter_keyboard_visibility"
    private var eventChannel: EventChannel? = null
    var mainView: View? = null
    var eventsSink: EventSink? = null
    var isVisible = false
    private var activity: FlutterActivity? = null
    private var binaryMessenger: BinaryMessenger? = null


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        binaryMessenger = flutterPluginBinding.binaryMessenger
        eventsSink = null
    }

    private fun unregisterListener() {
        if (mainView != null) {
            mainView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
            mainView = null
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        eventChannel?.setStreamHandler(null)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {

        // register listener
        this.eventsSink = events


        // is keyboard is visible at startup, let our subscriber know
        if (isVisible) {
            eventsSink!!.success(1)
        }
    }

    override fun onCancel(arguments: Any?) {
        this.eventsSink = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        try {
            mainView =
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            mainView?.viewTreeObserver?.addOnGlobalLayoutListener(this)
        } catch (e: Exception) {
            // do nothing
        }
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        unregisterListener()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        unregisterListener()
    }

    override fun onGlobalLayout() {
        val r = Rect()

        if (mainView != null) {
            mainView!!.getWindowVisibleDisplayFrame(r)

            // check if the visible part of the screen is less than 85%
            // if it is then the keyboard is showing
            val newState = r.height().toDouble() / mainView!!.rootView.height.toDouble() < 0.85
            if (newState != isVisible) {
                isVisible = newState
                if (eventsSink != null) {
                    eventsSink!!.success(if (isVisible) 1 else 0)
                }
            }
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity

        eventChannel = EventChannel(binaryMessenger, STREAM_CHANNEL_NAME)
        eventChannel?.setStreamHandler(this)

        activity?.application?.registerActivityLifecycleCallbacks(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = null
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
