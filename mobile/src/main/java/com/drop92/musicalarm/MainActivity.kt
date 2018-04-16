package com.drop92.musicalarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.drop92.musicalarm.fragment.PlaybackConfigFragment
import com.drop92.musicalarm.fragment.TimePickerFragment
import com.drop92.musicalarm.model.PlaybackType
import com.drop92.musicalarm.utils.Constants
import com.drop92.musicalarm.utils.PreferenceHelper
import com.drop92.musicalarm.utils.PreferenceHelper.setValue
import com.drop92.musicalarm.utils.PreferenceHelper.getValue
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity(), TimePickerFragment.OnTimePickerFragmentInteractionListener, PlaybackConfigFragment.OnPlaybackConfigFragmentInteractionListener {
    private val TAG = "!!!"

    private lateinit var am: AlarmManager
    private lateinit var con: Context
    private lateinit var sp: SharedPreferences
    private var playbackType: PlaybackType = PlaybackType.SMART_CHOICE_QUERY
    private var playbackQuery: String = Constants.FEELING_LUCKY_QUERY
    private var playbackNextSongTO: Long = Constants.NEXT_SONG_TO_DEFAULT

    private var pendingAlarmTimestampMs: Long = 0
    private var playbackNextSongSwitchIsEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_schedule_alarm.setOnClickListener {
            onScheduleButtonClick()
        }

        btn_cancel_alarm.setOnClickListener{
            onCancelAlarmButtonClick()
        }

        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        con = this
        sp = PreferenceHelper.defPreference(con)

        readInitData()

        //TBD: move into TimePicker or Scheduler
        var hh =""
        var mm =""

        if (pendingAlarmTimestampMs > System.currentTimeMillis()) {
            var formatterHours = SimpleDateFormat("HH")
            var formatterMinutes = SimpleDateFormat("mm")
            hh = formatterHours.format(pendingAlarmTimestampMs)
            mm = formatterMinutes.format(pendingAlarmTimestampMs)
        }

        var timeConfig: TimePickerFragment = TimePickerFragment.newInstance(hh,mm)

        supportFragmentManager.beginTransaction()
                .add(R.id.time_picker_fragment_container, timeConfig).commit()

        playbackType = PlaybackType.valueOf(sp.getValue(Constants.PREF_PLAYBACK_KEY, PlaybackType.SMART_CHOICE_QUERY.name))

        var plConfig: PlaybackConfigFragment = PlaybackConfigFragment.newInstance(playbackType, playbackQuery, playbackNextSongTO, playbackNextSongSwitchIsEnabled)
        supportFragmentManager.beginTransaction()
                .add(R.id.playback_config_fragment_container, plConfig).commit()
    }

    private fun readInitData() {
        playbackType = PlaybackType.valueOf(sp.getValue(Constants.PREF_PLAYBACK_KEY, PlaybackType.SMART_CHOICE_ANY.name))
        playbackQuery = sp.getValue(Constants.PREF_QUERY_KEY,Constants.FEELING_LUCKY_QUERY)
        pendingAlarmTimestampMs = sp.getValue(Constants.PREF_ALARM_TIME_MS, 0L)
        playbackNextSongTO = sp.getValue(Constants.PREF_SONG_TO_KEY, Constants.NEXT_SONG_TO_DEFAULT)
        playbackNextSongSwitchIsEnabled = sp.getValue(Constants.PREF_SONG_TO_SWITCH_STATE_KEY, false)
    }

    private fun setPlaybackType(newPlaybackType: PlaybackType) {
        playbackType = newPlaybackType
        sp.setValue(Constants.PREF_PLAYBACK_KEY, playbackType.name)
    }

    private fun setPlaybackQuery(newPlaybackQuery: String) {
        playbackQuery = newPlaybackQuery
        sp.setValue(Constants.PREF_QUERY_KEY, playbackQuery)
    }

    private fun setPlaybackTime(msTarget: Long) {
        pendingAlarmTimestampMs = msTarget
        //moved into actual scheduler
        //sp.setValue(Constants.PREF_ALARM_TIME_MS, pendingAlarmTimestampMs)
    }

    private fun setNextSongTO(newTO: Long) {
        playbackNextSongTO = newTO
        sp.setValue(Constants.PREF_SONG_TO_KEY, playbackNextSongTO)
    }

    private fun setNextSongSwitchState(isEnabled: Boolean) {
        playbackNextSongSwitchIsEnabled = isEnabled
        sp.setValue(Constants.PREF_SONG_TO_SWITCH_STATE_KEY, playbackNextSongSwitchIsEnabled)
    }

    private fun scheduleAlarm(msTargetTime: Long) {
        if (pendingAlarmTimestampMs > System.currentTimeMillis()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, msTargetTime, playbackType.getPendingIntent(con, playbackQuery))
            sp.setValue(Constants.PREF_ALARM_TIME_MS, pendingAlarmTimestampMs)
            this.toast("Music alarm set")
        } else {
            this.toast("Alarm TS is incorrect")
        }
    }

    private fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun onScheduleButtonClick () {
        scheduleAlarm(pendingAlarmTimestampMs)
    }

    private fun onCancelAlarmButtonClick() {
        //TBD: check that alarm is actually exist
        val pi = playbackType.getPendingIntent(con, playbackQuery)
        pi.cancel()
        am.cancel(pi)
        sp.setValue(Constants.PREF_ALARM_TIME_MS, System.currentTimeMillis())
        this.toast("Music alarm is canceled")
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

    //listeners
    override fun onAlarmTimestampChanged(timestamp: Long) {
        Log.d(TAG, "time: " + timestamp)
        setPlaybackTime(timestamp)
    }

    override fun onPlaybackTypeChanged(newType: PlaybackType) {
        Log.d(TAG, "type: " + newType)
        setPlaybackType(newType)
    }

    override fun onPlaybackQueryChanged(newQuery: String){
        Log.d(TAG, "query: " + newQuery)
        setPlaybackQuery(newQuery)
    }

    override fun onPlaybackNextSongTimeoutChanged(msNewTimeout: Long){
        Log.d(TAG, "2nd song timeout: " + msNewTimeout)
        setNextSongTO(msNewTimeout)
    }

    override fun onPlaybackNextSongSwitchChanged(isEnabled: Boolean){
        Log.d(TAG, "2nd song is enabled: " + isEnabled)
        setNextSongSwitchState(isEnabled)
    }
}

