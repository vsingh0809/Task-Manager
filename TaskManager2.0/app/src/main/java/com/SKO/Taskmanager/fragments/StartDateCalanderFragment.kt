package com.SKO.Taskmanager.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.annotation.RequiresApi
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.databinding.FragmentStartDateCalanderBinding
import com.SKO.Taskmanager.utils.ToastUtil
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

class StartDateCalanderFragment : Fragment() {

    private var binding : FragmentStartDateCalanderBinding ? = null

    // Interface for date selection communication
    interface OnDateSelectedListener {
        fun onDateSelected(date: String, localDateTime: LocalDateTime)
    }

    private var dateSelectedListener: OnDateSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure the host activity implements the interface
        if (context is OnDateSelectedListener) {
            dateSelectedListener = context
        } else {
//            throw RuntimeException("$context must implement OnDateSelectedListener")
            ToastUtil.showCustomToast(requireContext(), "$context must implement OnDateSelectedListener", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStartDateCalanderBinding.inflate(inflater,container, false)
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.startDateCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Create a Calendar instance with the selected date
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            // Format the date in the desired style
            val formattedDate = formatDate(calendar)

            // Convert to LocalDateTime
            val localDateTime = calendar.toLocalDateTime()

            // Notify the listener (MainActivity)
            dateSelectedListener?.onDateSelected(formattedDate, localDateTime)

            // Hide the calendar fragment
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    private fun formatDate(calendar: Calendar): String {
        // Create a function to add ordinal suffix to day
        fun getOrdinalDay(day: Int): String {
            return when {
                day in 11..13 -> "${day}th"
                day % 10 == 1 -> "${day}st"
                day % 10 == 2 -> "${day}nd"
                day % 10 == 3 -> "${day}rd"
                else -> "${day}th"
            }
        }

        // Format the date
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
        val year = calendar.get(Calendar.YEAR)

        return "${getOrdinalDay(dayOfMonth)} $monthName $year"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Calendar.toLocalDateTime(): LocalDateTime {
        return this.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    override fun onDetach() {
        super.onDetach()
        dateSelectedListener = null
    }
}