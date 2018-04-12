package com.drop92.musicalarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.drop92.musicalarm.fragment.PlaybackConfigFragment
import com.drop92.musicalarm.fragment.TimePickerFragment
import com.drop92.musicalarm.model.PlaybackType
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity(), TimePickerFragment.OnTimePickerFragmentInteractionListener, PlaybackConfigFragment.OnPlaybackConfigFragmentInteractionListener {
    private val FEELING_LUCKY_QUERY = ""
    private val NEXT_SONG_TO_DEFAULT = 4000L

    private lateinit var am: AlarmManager
    private lateinit var con: Context
    private var playbackType: PlaybackType = PlaybackType.SMART_CHOICE_QUERY
    private var playbackQuery: String = FEELING_LUCKY_QUERY
    private var nextSongTO: Long = NEXT_SONG_TO_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start_intent.setOnClickListener {
            onIntentButtonClick()
        }

        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        con = this
    }

    private fun setPlaybackType(newPlaybackType: PlaybackType) {
        playbackType = newPlaybackType
    }
    private fun setPlaybackQuery(newPlaybackQuery: String) {
        playbackQuery = newPlaybackQuery
    }

    private fun setNextSongTO(newTO: Long) {
        nextSongTO = newTO
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

    override fun onAlarmTimestampChanged(time: Long) {
        Log.d("!!!!", "fragment Stuff " + time)
    }

    override fun onPlaybackTypeChanged(newType: PlaybackType) {
        Log.d("!!!!", "fragment Stuff " + newType)

    }
    override fun onPlaybackQueryChanged(newQuery: String){
        Log.d("!!!!", "fragment Stuff " + newQuery)

    }
    override fun onPlaybackNextSongTimeoutChanged(msNewTimeout: Long){
        Log.d("!!!!", "fragment Stuff " + msNewTimeout)
    }
}

