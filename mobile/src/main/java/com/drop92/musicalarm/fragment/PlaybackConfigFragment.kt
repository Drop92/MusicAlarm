package com.drop92.musicalarm.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter

import com.drop92.musicalarm.R
import com.drop92.musicalarm.model.PlaybackType
import kotlinx.android.synthetic.main.fragment_playback_config.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnPlaybackConfigFragmentInteractionListener? = null

    private var pbQuery: String =""
    private var pbNextSongTimeout: Long = 4000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playback_config, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
       super.onActivityCreated(savedInstanceState)
        tapAnyButton()

        Thread({fetchPlaylists()}).start()

        playback_any_btn.setOnClickListener {
            tapAnyButton()
        }

        playback_query_btn.setOnClickListener {
            tapQueryButton("")
        }

        playback_playlist_btn.setOnClickListener {
            tapPlaylistButton("")
        }

        playback_next_song_switch.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked)
                    enableView(playback_2nd_song_timeout_edit_text)
                else
                    disableView(playback_2nd_song_timeout_edit_text)
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
                notifyPlaylistChanged(parentView.adapter.getItem(position).toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // your code here
            }
        }

        playback_2nd_song_timeout_edit_text.setOnEditorActionListener {
            _, keyCode, _ ->

            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                pbNextSongTimeout = playback_2nd_song_timeout_edit_text.text.toString().toLong()
                notifyNextSongTimeoutChanged(pbNextSongTimeout)
                true
            }
            false
        }
    }

    private fun tapAnyButton() {
        setAnyUI()
        notifyQueryChanged("")
        notifyPlaybackTypeChanged(PlaybackType.SMART_CHOICE_QUERY)
    }

    private fun tapQueryButton(query: String) {
        setActiveQueryButton()
        notifyQueryChanged(pbQuery)
        notifyPlaybackTypeChanged(PlaybackType.SMART_CHOICE_QUERY)
    }

    private fun tapPlaylistButton(playlistName: String) {
        setActivePlaylistButton()
        notifyPlaylistChanged(getSelectedPlaylist())
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

        if (playback_next_song_switch.isChecked)
            enableView(playback_2nd_song_timeout_edit_text)
        else
            disableView(playback_2nd_song_timeout_edit_text)
    }

    private fun setActiveQueryButton() {
        playback_any_btn.setBackgroundResource(R.drawable.playback_fragment_button)
        playback_query_btn.setBackgroundResource(R.drawable.playback_fragment_button_pressed)
        playback_playlist_btn.setBackgroundResource(R.drawable.playback_fragment_button)

        enableView(playback_query_edit_text)
        disableView(playback_playlist_spinner)
        disableView(playback_next_song_switch)
        disableView(playback_2nd_song_timeout_edit_text)
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
        activity?.runOnUiThread({playback_playlist_spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, playlists) })
    }

    private fun findPlaylists(): ArrayList<String> {
        var playLists = arrayListOf<String>()
        val projection = arrayOf("playlist_name")
        val playlistUri = Uri.parse("content://com.google.android.music.MusicContent/playlists")
        val playlistCursor = context?.contentResolver?.query(playlistUri, projection, null, null, null)
        if (playlistCursor!!.count > 0) {
            playlistCursor.moveToFirst()
            do {
                playLists.add(playlistCursor.getString(0))
            } while (playlistCursor.moveToNext())
        }
        return playLists
    }

    private fun getSelectedPlaylist(): String {
        return playback_playlist_spinner.selectedItem.toString()
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnPlaybackConfigFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onPlaybackTypeChanged(newType: PlaybackType)
        fun onPlaybackQueryChanged(newQuery: String)
        fun onPlaybackNextSongTimeoutChanged(msNewTimeout: Long)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaybackConfigFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PlaybackConfigFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
