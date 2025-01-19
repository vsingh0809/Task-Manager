package com.SKO.Taskmanager.utils
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.SKO.Taskmanager.R

object ToastUtil {

    fun showCustomToast(context:Context, message: String, isSuccess: Boolean) {
        // Inflate the custom toast layout
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.custom_toast, null)

        // Set the message
        val textView: TextView = view.findViewById(R.id.toast_message)
        textView.text = message

        // Set the icon and background color based on success or failure
        val icon: ImageView = view.findViewById(R.id.toast_icon)
        if (isSuccess) {
            icon.setImageResource(R.drawable.success) // Add your success icon in res/drawable
            view.setBackgroundColor(Color.GREEN)
        } else {
            icon.setImageResource(R.drawable.cancel) // Add your failure icon in res/drawable
            view.setBackgroundColor(Color.RED)
        }

        icon.visibility = View.VISIBLE
        // Create and show the Toast
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = view
        toast.show()
    }
}
