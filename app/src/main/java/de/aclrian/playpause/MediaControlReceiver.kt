package de.aclrian.playpause

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Looper
import android.os.SystemClock
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.text.HtmlCompat
import java.util.*

private const val IS_BUTTON_PRESSED = "com.samsung.android.knox.intent.extra.KEY_REPORT_TYPE"
private const val BUTTON_TYPE = "com.samsung.android.knox.intent.extra.KEY_CODE"
private const val BUTTON_DOWN = 1
private const val BUTTON_UP = 2

class MediaControlReceiver : BroadcastReceiver() {
    private var startTime = -1L

    enum class Duration {
        SHORT, MEDIUM, LONG, EXTRA_LONG
    }


    override fun onReceive(context: Context, intent: Intent) {
        val keyCode = intent.getIntExtra(BUTTON_TYPE, -BUTTON_DOWN)
        val keyReportType = intent.getIntExtra(IS_BUTTON_PRESSED, -BUTTON_DOWN)
        if (keyCode == 1015) {
            if (keyReportType == BUTTON_DOWN) {
                startTime = SystemClock.uptimeMillis()
            }
            else if (keyReportType == BUTTON_UP && startTime > 0) {
                val endTime = SystemClock.uptimeMillis()
                val duration = endTime - startTime
                if (duration < 1000L) {
                    Thread { Looper.prepare(); act(context, Duration.SHORT) }.start()
                } else if (duration < 3000L) {
                    Thread { Looper.prepare(); act(context, Duration.MEDIUM) }.start()
                } else if (duration < 6000L) {
                    Thread { Looper.prepare(); act(context, Duration.LONG) }.start()
                } else {
                    Thread { Looper.prepare(); act(context, Duration.EXTRA_LONG) }.start()
                }
                startTime = -1L
            }
        }
    }

    private fun act(context: Context, duration: Duration) {
        Log.i("PlayPause", "act " + duration.name)
        val audioManager =
            context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val eventTime = SystemClock.uptimeMillis()

        // check if the button press was intended -> should the action be performed?
        val isHeadsetConnected =
            Arrays.stream(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))
                .noneMatch {
                    it.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                            && it.type != AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                }
        if (isHeadsetConnected || audioManager.availableCommunicationDevices.size == 0) {
            Toast.makeText(context, "skipped", Toast.LENGTH_SHORT).show()
        } else {
            when (duration) {
                Duration.SHORT -> {
                    pressPlayPauseButton(eventTime, audioManager)
                }
                Duration.MEDIUM -> {
                    pressNextSongButton(eventTime, audioManager)
                }
                Duration.LONG -> {
                    pressPreviousSongButton(eventTime, audioManager)
                }
                Duration.EXTRA_LONG -> {}
            }
            val message =
                "Action <small><i>" + duration.name.lowercase() + "</i></small>  performed"
            Toast.makeText(
                context,
                Html.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun pressPreviousSongButton(
        event_time: Long,
        audioManager: AudioManager
    ) {
        val prevDownEvent = KeyEvent(
            event_time,
            event_time,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            0
        )
        audioManager.dispatchMediaKeyEvent(prevDownEvent)
        val prevUpEvent = KeyEvent(
            event_time,
            event_time,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            0
        )
        audioManager.dispatchMediaKeyEvent(prevUpEvent)
    }

    private fun pressNextSongButton(event_time: Long, audioManager: AudioManager) {
        val nextDownEvent = KeyEvent(
            event_time,
            event_time,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            0
        )
        audioManager.dispatchMediaKeyEvent(nextDownEvent)
        val nextUpEvent = KeyEvent(
            event_time,
            event_time,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            0
        )
        audioManager.dispatchMediaKeyEvent(nextUpEvent)
    }

    private fun pressPlayPauseButton(
        event_time: Long,
        audioManager: AudioManager
    ) {
        val ppDownEvent = KeyEvent(
            event_time,
            event_time,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            0
        )
        audioManager.dispatchMediaKeyEvent(ppDownEvent)
        val ppUpEvent = KeyEvent(
            event_time,
            event_time,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            0
        )
        audioManager.dispatchMediaKeyEvent(ppUpEvent)
    }
}