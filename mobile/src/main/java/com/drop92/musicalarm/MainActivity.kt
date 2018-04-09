package com.drop92.musicalarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.drop92.musicalarm.model.PlaybackType
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    private val TAG = "Debug"
    private val FEELING_LUCKY_QUERY = ""
    private val NEXT_SONG_TIMEOUT = 5000

    private lateinit var am: AlarmManager
    private lateinit var con: Context
    private lateinit var playbackType: PlaybackType
    private lateinit var playbackQuery: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start_intent.setOnClickListener{
            onIntentButtonClick()
        }

        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        con = this

        setPlaybackQuery(FEELING_LUCKY_QUERY)
        setPlaybackType(PlaybackType.SMART_CHOICE_QUERY)
    }

    private fun setPlaybackType(newPlaybackType: PlaybackType) {
        playbackType = newPlaybackType
    }
    private fun setPlaybackQuery(newPlaybackQuery: String) {
        playbackQuery = newPlaybackQuery
    }

    private fun scheduleAlarm(msTimeout: Long) {
        var msTarget = System.currentTimeMillis() + msTimeout
        am.set(AlarmManager.RTC_WAKEUP, msTarget, playbackType.getPendingIntent(con, playbackQuery))
    }

    private fun cancelAlarm() {
        val pi = playbackType.getPendingIntent(con, playbackQuery)
        pi.cancel()
        am.cancel(pi)
    }
    
    private fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun onIntentButtonClick () {
        this.toast("Music alarm in 5 minutes")
        //for debug five minute after start app
        scheduleAlarm(5 * 60 * 1000)
    }

    private fun findPlaylists() {
        var play: String =""
        val proj = arrayOf("playlist_name")
        val playlistUri = Uri.parse("content://com.google.android.music.MusicContent/playlists")
        val playlistCursor = contentResolver.query(playlistUri, proj, null, null, null)
        if (playlistCursor!!.count > 0) {
            playlistCursor.moveToFirst()
            do {
                play =  playlistCursor.getString(0)
                Log.d (TAG, play)
            } while (playlistCursor.moveToNext())
        }
    }
    
    private fun playNextSong(msTimeout: Long) {
        //workaround to play next song from GM instead of first (it is annoying that GM can't shuffle playlist prior playing)
        //TBD check how it works, probably it can be easier to set up first track as "10 sec silence"
        val timer = Timer()
        timer.schedule(timerTask { requestNextSong()}, msTimeout)
    }

    private fun requestNextSong() {
       val i = Intent("com.android.music.musicservicecommand")
       i.putExtra("command", "next")
       sendBroadcast(i)
    }
}

