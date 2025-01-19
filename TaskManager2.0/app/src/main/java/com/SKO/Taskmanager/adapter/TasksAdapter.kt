package com.SKO.Taskmanager.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.fragments.HomeFragment
import com.SKO.Taskmanager.responses.TaskDataClass
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TasksAdapter(private var tasks: ArrayList<TaskDataClass>, private val homeFragment: HomeFragment, private var context: Context,
                   private val onItemClickListener: (TaskDataClass) -> Unit,
                   private val onEditClickListener: (TaskDataClass) -> Unit): RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    @SuppressLint("NewApi")
    val currentDateTime = LocalDateTime.now()

    @SuppressLint("NewApi")
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    @SuppressLint("NewApi")
    val starttDate = currentDateTime.format(formatter)

    private var isItemClickDisabled = false


    fun disableItemListeners(disable: Boolean) {
        isItemClickDisabled = disable
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_card_layout_2, parent, false)

        return TaskViewHolder(view, homeFragment)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        if (isItemClickDisabled) {
            holder.itemView.setOnClickListener(null)
            //holder.itemView.findViewById<View>(R.id.edit_button).setOnClickListener(null)
        } else {
            holder.itemView.setOnClickListener {
                onItemClickListener(task)
            }
//            holder.itemView.findViewById<View>(R.id.edit_button).setOnClickListener {
//                onEditClickListener(task)
//            }
        }


        holder.Title.text = if (task.title.length > 40) {
            task.title.substring(0, 39) + "..."
        } else {
            task.title
        }
        holder.Status.text = task.status
        holder.StartDate.text = LocalDateTime.parse(task.startDate).format(formatter)
        holder.EndDate.text = LocalDateTime.parse(task.endDate).format(formatter)

        when (task.status) {
            "To Do" -> holder.Status.setTextColor(Color.GRAY)
            "Completed" -> holder.Status.setTextColor(Color.GREEN)
            "On Hold" -> holder.Status.setTextColor(Color.RED)
            "In Progress" -> holder.Status.setTextColor(homeFragment.resources.getColor(R.color.dark_yellow, context.theme))
            else -> holder.Status.setTextColor(Color.TRANSPARENT)
        }


        if (!homeFragment.isContexualModeEnabled) {
            holder.checkBox.visibility = View.GONE
        } else {
            holder.checkBox.visibility = View.VISIBLE

            // Check if this task is selected (in multiDeleteList)
            holder.checkBox.isChecked = homeFragment.multiDeleteList.contains(task)

            // Set an onClick listener for the checkbox
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!homeFragment.multiDeleteList.contains(task)) {
                        homeFragment.multiDeleteList.add(task) // Add task to selected list
                    }
                }else {
                    homeFragment.multiDeleteList.remove(task) // Remove task from selected list
                }
                homeFragment.updateCounter()  // Update the counter UI
            }
        }

    }

    inner class TaskViewHolder(itemView: View, homeFragment: HomeFragment) : RecyclerView.ViewHolder(itemView) , View.OnClickListener{

        var Title : TextView = itemView.findViewById(R.id.textViewTaskCardTitle)
        var Status : TextView = itemView.findViewById(R.id.textViewTaskCardStatus)
        var StartDate: TextView = itemView.findViewById(R.id.textViewTaskCardStartDateText)
        var EndDate: TextView = itemView.findViewById(R.id.textViewTaskCardEndDateText)
        var checkBox : CheckBox = itemView.findViewById(R.id.checkBox)
        var statusIndicator: View = itemView.findViewById(R.id.textViewTaskCardStatus)
        var view : View = itemView


        init {
            itemView.setOnClickListener {
                val task = tasks[adapterPosition]
                onItemClickListener(task)
            }
            view.setOnLongClickListener(homeFragment)
            checkBox.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            homeFragment.makeSelection(view, adapterPosition)
        }
    }

    fun setTasks(tasks: List<TaskDataClass>) {
        this.tasks = ArrayList(tasks)
        notifyDataSetChanged()
    }

    fun getTasks(): List<TaskDataClass> {
        return tasks
    }

}