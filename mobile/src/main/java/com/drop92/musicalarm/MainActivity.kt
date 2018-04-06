package com.drop92.musicalarm

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    private val TAG = "Debug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start_intent.setOnClickListener{
            onIntentButtonClick()
        }
    }

    private fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun onIntentButtonClick () {
        this.toast("IntentIsStarted")
        //startAnyMusic()
        //startPlaylistMusic("Easy")
        findPlaylists()
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
            } while (playlistCursor.moveToNext())
        }

        Log.d (TAG, play)
        startPlaylistMusic(play)
    }

    private fun startAnyMusic() {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,"vnd.android.cursor.item/*")
        intent.putExtra(SearchManager.QUERY, "")
        startActivity(intent)
    }

    private fun startPlaylistMusic(playlistName: String) {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE)
        //for some reason this prevents playlist selection
        //intent.putExtra(MediaStore.EXTRA_MEDIA_PLAYLIST, playlistName)

        //workaround for GM playlist selection -drawback playlist name can accidentally match with something unpredictable
        intent.putExtra(SearchManager.QUERY, playlistName)

        Log.d (TAG, "" + intent.extras)
        startActivity(intent)

        //workaround to play next song from GM instead of first (it is annoying that GM can't shuffle playlist prior playing)
        //TBD check how it works, probably it can be easier to set up first track as "10 sec silence"
        val timer = Timer()
        timer.schedule(timerTask { nextSong()  }, 5000)
    }

    private fun nextSong() {

       val i = Intent("com.android.music.musicservicecommand")
       i.putExtra("command", "next")
       sendBroadcast(i)
    }
}

