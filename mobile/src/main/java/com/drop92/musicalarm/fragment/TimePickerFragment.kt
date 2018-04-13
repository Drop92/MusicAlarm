package com.drop92.musicalarm.fragment

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker

import com.drop92.musicalarm.R
import kotlinx.android.synthetic.main.fragment_time_picker.*
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_HOURS = "arg_hh"
private const val ARG_MINUTES = "arg_mm"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TimePickerFragment.OnTimePickerFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TimePickerFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TimePickerFragment : Fragment(), TimePickerDialog.OnTimeSetListener {
    private var hours: Int? = null
    private var minutes: Int? = null
    private var listener: OnTimePickerFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            try {
                if (it.getString(ARG_HOURS).toInt() < 23 && it.getString(ARG_MINUTES).toInt() < 59) {
                    hours = it.getString(ARG_HOURS).toInt()
                    minutes = it.getString(ARG_MINUTES).toInt()
                }
            } catch (e: NumberFormatException) {
                hours = null
                minutes = null
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        hours?.let{time_picker_hours.text = String.format("%02d", it)}
        minutes?.let{time_picker_minutes.text = String.format("%02d", it)}

        time_picker_widget.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val tpd = TimePickerDialog(activity,this, hour, minute, true)
            tpd.show()
        }
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        time_picker_hours.text = String.format("%02d", hourOfDay)
        time_picker_minutes.text = String.format("%02d", minute)
        hours = hourOfDay
        minutes = minute

        var date = Date(System.currentTimeMillis())
        var formatter = SimpleDateFormat("yyyy/MM/dd")
        val today = formatter.format(date)

        var resultAlarmTimestamp:Long = formatter.parse(today).time

        resultAlarmTimestamp += hourOfDay * 60 * 60 * 1000 + minute * 60 * 1000

        if (resultAlarmTimestamp < System.currentTimeMillis())
            resultAlarmTimestamp += 24 * 60 * 60 * 1000

        notifyAlarmTimestampChanged(resultAlarmTimestamp)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_picker, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTimePickerFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTimePickerFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun notifyAlarmTimestampChanged(newTs: Long) {
        listener?.onAlarmTimestampChanged(newTs)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnTimePickerFragmentInteractionListener {
        fun onAlarmTimestampChanged(timestamp: Long)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param hh hours - 00 to 23.
         * @param mm minutes - 00 to 59.
         * @return A new instance of fragment TimePickerFragment.
         */

        @JvmStatic
        fun newInstance(hh: String, mm: String) =
            TimePickerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_HOURS, hh)
                    putString(ARG_MINUTES, mm)
                }
            }
    }
}
