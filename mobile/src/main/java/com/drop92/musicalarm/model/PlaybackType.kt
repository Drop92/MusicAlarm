package com.drop92.musicalarm.model

import android.app.PendingIntent
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.provider.MediaStore

enum class PlaybackType {
    //Query to find something or use "" for //GM feeling lucky radio/resume of prev session
    SMART_CHOICE_QUERY {
        override fun getMediaStoreIntent(query: String): Intent {
            val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
            intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,"vnd.android.cursor.item/*")
            intent.putExtra(SearchManager.QUERY, query)
            return intent
        }
    },

    //under investigation. Shuffle starts from second song, first song is always the same. Workaround may be required
    PLAYLIST_SHUFFLE {
        override fun getMediaStoreIntent(query: String): Intent {
            val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
            intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE)
            //for some reason this prevents playlist selection
            //intent.putExtra(MediaStore.EXTRA_MEDIA_PLAYLIST, playlistName)

            //workaround for GM playlist selection -drawback playlist name can accidentally match with something unpredictable
            intent.putExtra(SearchManager.QUERY, query)
            return intent
        }
    },

    //under investigation. May be will be deleted later.
    PLAYLIST_CONS {
        override fun getMediaStoreIntent(query: String): Intent {
            val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
            intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE)
            //for some reason this prevents playlist selection
            //intent.putExtra(MediaStore.EXTRA_MEDIA_PLAYLIST, playlistName)

            //workaround for GM playlist selection -drawback playlist name can accidentally match with something unpredictable
            intent.putExtra(SearchManager.QUERY, query)
            return intent
        }
    };

    abstract fun getMediaStoreIntent(query: String): Intent
    private val requestCode = 1408

    fun getPendingIntent(ctx: Context, query: String): PendingIntent {
        return PendingIntent.getActivity(ctx, requestCode, getMediaStoreIntent(query), PendingIntent.FLAG_UPDATE_CURRENT)
        //https://developer.android.com/reference/android/app/PendingIntent.html#FLAG_UPDATE_CURRENT
    }
}