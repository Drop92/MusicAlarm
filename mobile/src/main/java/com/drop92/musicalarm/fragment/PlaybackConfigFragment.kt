package com.drop92.musicalarm.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*

import com.drop92.musicalarm.R
import com.drop92.musicalarm.model.PlaybackType
import kotlinx.android.synthetic.main.fragment_playback_config.*
import android.widget.AdapterView.OnItemSelectedListener
import com.drop92.musicalarm.utils.Constants

private const val ARG_PL_TYPE = "playback_type"
private const val ARG_PL_QUERY = "playback_query"
private const val ARG_PL_NEXT_SONG_TO = "playback_next_song_to"
private const val ARG_PL_NEXT_SONG_SWITCH_IS_ENABLED = "playback_next_song_switch_state"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PlaybackConfigFragment.OnPlaybackConfigFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PlaybackConfigFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PlaybackConfigFragment : Fragment() {
    private var listener: OnPlaybackConfigFragmentInteractionListener? = null

    //defaults
    private var pbType: PlaybackType = PlaybackType.SMART_CHOICE_ANY
    private var pbQuery: String = Constants.FEELING_LUCKY_QUERY
    private var pbNextSongTimeout: Long = Constants.NEXT_SONG_TO_DEFAULT
    private var pbNextSongSwitchIsEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pbType = it.getSerializable(ARG_PL_TYPE) as PlaybackType
            pbQuery = it.getString(ARG_PL_QUERY)
            pbNextSongTimeout = it.getLong(ARG_PL_NEXT_SONG_TO)
            pbNextSongSwitchIsEnabled = it.getBoolean(ARG_PL_NEXT_SONG_SWITCH_IS_ENABLED)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playback_config, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
       super.onActivityCreated(savedInstanceState)

        Thread({fetchPlaylists()}).start()

        playback_any_btn.setOnClickListener {
            tapAnyButton()
        }

        playback_query_btn.setOnClickListener {
            tapQueryButton()
        }

        playback_playlist_btn.setOnClickListener {
            tapPlaylistButton()
        }

        playback_next_song_switch.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) {
                    enableView(playback_2nd_song_timeout_edit_text)
                    pbNextSongSwitchIsEnabled = true
                    notifyPlaybackNextSongSwitchChanged(pbNextSongSwitchIsEnabled)
                } else {
                    disableView(playback_2nd_song_timeout_edit_text)
                    pbNextSongSwitchIsEnabled = false
                    notifyPlaybackNextSongSwitchChanged(pbNextSongSwitchIsEnabled)
                }
            }
        }

        playback_query_edit_text.setOnEditorActionListener {
            _, keyCode, _ ->

            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                pbQuery = playback_query_edit_text.text.toString()
                notifyQueryChanged(pbQuery)
                true
            }
            false
        }

        playback_playlist_spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                if(playback_playlist_spinner.isClickable)
                    notifyPlaylistChanged(parentView.adapter.getItem(position).toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }

        playback_2nd_song_timeout_edit_text.setOnEditorActionListener {
            _, keyCode, _ ->

            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                try {
                    pbNextSongTimeout = playback_2nd_song_timeout_edit_text.text.toString().toLong()
                    notifyNextSongTimeoutChanged(pbNextSongTimeout)
                } catch (e: NumberFormatException){
                    //fall back to default
                    pbNextSongTimeout = Constants.NEXT_SONG_TO_DEFAULT
                    playback_2nd_song_timeout_edit_text.setText(pbNextSongTimeout.toString(), TextView.BufferType.EDITABLE)
                }
            }
            false
        }

        initFragment()
    }

    private fun initFragment() {
        when (pbType) {
            PlaybackType.SMART_CHOICE_ANY -> {
                setAnyUI()
            }

            PlaybackType.SMART_CHOICE_QUERY -> {
                playback_query_edit_text.setText(pbQuery, TextView.BufferType.EDITABLE)
                setActiveQueryButton()
            }

            PlaybackType.PLAYLIST_SHUFFLE -> {
                setActivePlaylistButton()
            }

            PlaybackType.PLAYLIST_CONS -> {
                setActivePlaylistButton()
            }
        }

        playback_2nd_song_timeout_edit_text.setText(pbNextSongTimeout.toString(), TextView.BufferType.EDITABLE)
    }

    private fun tapAnyButton() {
        setAnyUI()
        notifyQueryChanged(Constants.FEELING_LUCKY_QUERY)
        notifyPlaybackTypeChanged(PlaybackType.SMART_CHOICE_ANY)
    }

    private fun tapQueryButton() {
        setActiveQueryButton()
        notifyQueryChanged(pbQuery)
        notifyPlaybackTypeChanged(PlaybackType.SMART_CHOICE_QUERY)
    }

    private fun tapPlaylistButton() {
        setActivePlaylistButton()
        notifyPlaylistChanged(getSelectedPlaylist()?: Constants.FEELING_LUCKY_QUERY)
        notifyPlaybackTypeChanged(PlaybackType.PLAYLIST_SHUFFLE)
    }

    private fun setAnyUI(){
        playback_any_btn.setBackgroundResource(R.drawable.playback_fragment_button_pressed)
        playback_query_btn.setBackgroundResource(R.drawable.playback_fragment_button)
        playback_playlist_btn.setBackgroundResource(R.drawable.playback_fragment_button)

        disableView(playback_query_edit_text)
        disableView(playback_playlist_spinner)
        disableView(playback_next_song_switch)
        disableView(playback_2nd_song_timeout_edit_text)
    }

    private fun setActivePlaylistButton(){
        playback_any_btn.setBackgroundResource(R.drawable.playback_fragment_button)
        playback_query_btn.setBackgroundResource(R.drawable.playback_fragment_button)
        playback_playlist_btn.setBackgroundResource(R.drawable.playback_fragment_button_pressed)

        disableView(playback_query_edit_text)
        enableView(playback_playlist_spinner)
        enableView(playback_next_song_switch)

        playback_next_song_switch.isChecked = pbNextSongSwitchIsEnabled

        if (playback_next_song_switch.isChecked) {
            enableView(playback_2nd_song_timeout_edit_text)
        } else {
            disableView(playback_2nd_song_timeout_edit_text)
        }
    }

    private fun setActiveQueryButton() {
        playback_any_btn.setBackgroundResource(R.drawable.playback_fragment_button)
        playback_query_btn.setBackgroundResource(R.drawable.playback_fragment_button_pressed)
        playback_playlist_btn.setBackgroundResource(R.drawable.playback_fragment_button)

        enableView(playback_query_edit_text)
        disableView(playback_playlist_spinner)
        disableView(playback_next_song_switch)
    }

    private fun disableView(v: View) {
        v.alpha = 0.5f
        v.isEnabled = false
        v.isClickable = false
    }

    private fun enableView(v: View) {
        v.alpha = 1f
        v.isEnabled = true
        v.isClickable = true
    }

    private fun fetchPlaylists() {
        var playlists = findPlaylists()
        activity?.runOnUiThread({
            playback_playlist_spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, playlists)
            for (playlistPosition in 0 until playback_playlist_spinner.adapter.count) {
                var item = playback_playlist_spinner.adapter.getItem(playlistPosition)
                if (item == pbQuery) {
                    playback_playlist_spinner.setSelection(playlistPosition)
                }
            }
        })
    }

    private fun findPlaylists(): ArrayList<String> {
        var playLists = arrayListOf<String>()
        val projection = arrayOf("playlist_name")
        val playlistUri = Uri.parse("content://com.google.android.music.MusicContent/playlists")
        val playlistCursor = context?.contentResolver?.query(playlistUri, projection, null, null, null)

        playlistCursor?.let {
            if (it!!.count > 0) {
                it.moveToFirst()
                do {
                    playLists.add(it.getString(0))
                } while (it.moveToNext())
            }
        }
        return playLists
    }

    private fun getSelectedPlaylist(): String? {
        playback_playlist_spinner.selectedItem?.let{
            return it.toString()
        }?: run {
            return null
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPlaybackConfigFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTimePickerFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun notifyPlaylistChanged(plQuery: String) {
        listener?.onPlaybackQueryChanged(plQuery)
    }

    private fun notifyQueryChanged(query: String) {
        listener?.onPlaybackQueryChanged(query)
    }

    private fun notifyNextSongTimeoutChanged(msTimeout: Long) {
        listener?.onPlaybackNextSongTimeoutChanged(msTimeout)
    }

    private fun notifyPlaybackTypeChanged(newType: PlaybackType) {
        listener?.onPlaybackTypeChanged(newType)
    }

    private fun notifyPlaybackNextSongSwitchChanged(isEnabled: Boolean) {
        listener?.onPlaybackNextSongSwitchChanged(isEnabled)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     */
    interface OnPlaybackConfigFragmentInteractionListener {
        fun onPlaybackTypeChanged(newType: PlaybackType)
        fun onPlaybackQueryChanged(newQuery: String)
        fun onPlaybackNextSongTimeoutChanged(msNewTimeout: Long)
        fun onPlaybackNextSongSwitchChanged(isEnabled: Boolean)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param plbkType Parameter 1.
         * @param plbkQuery Parameter 2.
         * @param plbkNextSongTO Parameter 3.
         * @param plbkNextSongSwitchIsEnabled Parameter 4
         * @return A new instance of fragment PlaybackConfigFragment.
         */

        @JvmStatic
        fun newInstance(plbkType: PlaybackType, plbkQuery: String, plbkNextSongTO: Long, plbkNextSongSwitchIsEnabled: Boolean) =
                PlaybackConfigFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PL_TYPE, plbkType)
                        putString(ARG_PL_QUERY, plbkQuery)
                        putLong(ARG_PL_NEXT_SONG_TO, plbkNextSongTO)
                        putBoolean(ARG_PL_NEXT_SONG_SWITCH_IS_ENABLED, plbkNextSongSwitchIsEnabled)
                    }
                }
    }
}
