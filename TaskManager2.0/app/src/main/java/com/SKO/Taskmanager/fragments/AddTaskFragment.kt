package com.SKO.Taskmanager.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.classes.addTaskResponse
import com.SKO.Taskmanager.databinding.FragmentAddTaskBinding
import com.SKO.Taskmanager.interfaces.AddTaskApiInterface
import com.SKO.Taskmanager.interfaces.TaskApiInterface
import com.SKO.Taskmanager.responses.AddTaskRequest
import com.SKO.Taskmanager.utils.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskFragment : Fragment() {
    private  var binding: FragmentAddTaskBinding?=null
    //private lateinit var taskAPIInterface: TaskApiInterface
    private var addTaskApiInterface : AddTaskApiInterface ? = null
    private var selectedDueDate: Date? = null  // This will store the selected date
    private var toolbar : androidx.appcompat.widget.Toolbar ?= null
    private var taskId:Int?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        toolbar = binding?.toolbarInclude?.toolbar

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize API client
        addTaskApiInterface =
            ApiClient.getMainClient(requireContext()).create(AddTaskApiInterface::class.java)

        binding?.progressBarAddTask?.visibility = View.GONE
        binding?.textInputEditTextDueDate?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePicker()
            }
        }

        // Set up click listener as a fallback
        binding?.textInputEditTextDueDate?.setOnClickListener {
            showDatePicker()
        }

        // Set up click listener for due date field
        binding?.textInputEditTextDueDate?.setOnClickListener {
            showDatePicker()
        }

        binding?.buttonAddTask?.setOnClickListener {
            validateAndAddTask()

        }
    }

    private fun showDatePicker() {
        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Set the selected date in the calendar object
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)

                // Store the selected date
                selectedDueDate = calendar.time

                // Format the selected date correctly and set it in the EditText
                val selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDueDate)
                binding?.textInputEditTextDueDate?.setText(selectedDate)
            },
            year, month, day
        )

        // Disable past dates by setting minimum date
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }


    private fun validateAndAddTask() {
        val title = binding?.textInputEditTextTitle?.text.toString().trim()
        val description = binding?.textInputEditTextDesc?.text.toString().trim()
        val dueDate = binding?.textInputEditTextDueDate?.text.toString().trim()

        // Validate inputs
        var isValid = true

        if (ValidationUtils.isEmpty(title)) {
            binding?.textInputLayoutTitle?.error = getString(R.string.title_is_required_text)
            binding?.textInputEditTextTitle?.requestFocus()
            isValid = false
        } else {
            binding?.textInputLayoutTitle?.error = null
        }

        if (ValidationUtils.isEmpty(dueDate)) {
            binding!!.textInputLayoutDueDate.error = getString(R.string.due_date_is_required_text)
            binding!!.textInputEditTextDueDate.requestFocus()
            isValid = false
        } else {
            binding!!.textInputLayoutDueDate.error = null
        }

        if (ValidationUtils.isEmpty(description)) {
            binding!!.textInputLayoutDes.error = getString(R.string.description_is_required_text)
            binding!!.textInputEditTextDesc.requestFocus()
            isValid = false
        } else {
            binding!!.textInputLayoutDes.error = null
        }

        if (isValid && selectedDueDate != null) {
            // Show progress bar while adding task
            binding!!.progressBarAddTask.visibility = View.VISIBLE

            // Format the selected due date for the backend (using a specific format)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formattedDate = sdf.format(selectedDueDate)

            // Create AddTaskRequest object
            val addTaskRequest = AddTaskRequest(title, description, formattedDate)

            // Make API call to add the task
            val call: Call<addTaskResponse> ?= addTaskApiInterface?.addTask(addTaskRequest)
            call?.enqueue(object : Callback<addTaskResponse> {
                override fun onResponse(
                    call: Call<addTaskResponse>,
                    response: Response<addTaskResponse>
                ) {
                    if (response.isSuccessful) {
                        context?.let {
                            ToastUtil.showCustomToast(it, getString(R.string.task_created_successfully_toast), true)
                        }
                       taskId=response.body()?.task?.taskId
                        val action=AddTaskFragmentDirections.actionAddTaskFragmentToViewTaskFragment(taskId!!)
                        findNavController().navigate(action)
                    } else {
                        context?.let { ToastUtil.showCustomToast(it, getString(R.string.retry_failed_to_create_task_toast), false) }
                    }
                    binding!!.progressBarAddTask.visibility = View.GONE
                }

                override fun onFailure(call: Call<addTaskResponse>, t: Throwable) {
                    context?.let { ToastUtil.showCustomToast(it, " ${t.message}", false) }
                    binding?.progressBarAddTask?.visibility = View.GONE
                }
            })
        } else {
            // Handle case where no date is selected
            context?.let { ToastUtil.showCustomToast(it, getString(R.string.please_select_a_valid_date_toast), false) }
        }
    }


    object ValidationUtils {
        fun isEmpty(input: String?): Boolean {
            return input.isNullOrBlank()
        }
    }
}