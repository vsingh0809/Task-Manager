package com.SKO.Taskmanager.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.databinding.DialogSaveTaskBinding
import com.SKO.Taskmanager.databinding.FragmentEditTaskBinding
import com.SKO.Taskmanager.interfaces.EditTaskApiInterface
import com.SKO.Taskmanager.interfaces.GetSingleTaskApiInterface
import com.SKO.Taskmanager.interfaces.TaskApiInterface
import com.SKO.Taskmanager.responses.ApiResponse
import com.SKO.Taskmanager.responses.EditTaskRequest
import com.SKO.Taskmanager.responses.EditTaskResponse
import com.SKO.Taskmanager.utils.DateFormater
import com.SKO.Taskmanager.utils.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTaskFragment : Fragment() {
    private val dateFormater = DateFormater()
   // private var taskApiInterface: TaskApiInterface? = null
    private var editTaskApiInterface : EditTaskApiInterface?= null
    private var getSingleTaskApiInterface : GetSingleTaskApiInterface?= null
    private var taskId: Int? = null
    private var selectedDueDate: Date? = null
    private var binding: FragmentEditTaskBinding? = null

    private var toolbar : androidx.appcompat.widget.Toolbar ?= null

    private val statusOptions = arrayOf("To Do", "Completed", "On Hold", "In Progress")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        toolbar = binding?.toolbarInclude?.toolbar

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.textInputEditTextStartDate?.isEnabled = false

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item,
            statusOptions
        )
        binding?.statusDropdown?.setAdapter(adapter)

        editTaskApiInterface = ApiClient.getMainClient(requireActivity()).create(EditTaskApiInterface::class.java)
        getSingleTaskApiInterface = ApiClient.getMainClient(requireActivity()).create(GetSingleTaskApiInterface::class.java)

        binding?.textInputEditTextDueDate?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePicker()
            }
        }
        binding?.textInputEditTextDueDate?.setOnClickListener {
            showDatePicker()
        }

        taskId = arguments?.getInt("taskIdEdit")
        getTaskDetails()

        // Title validation as user types
        binding?.textInputEditTextTitle?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val title = charSequence.toString().trim()
                if (title.isEmpty()) {
                    binding?.textInputLayoutTitle?.error = getString(R.string.Title_is_required_text)
                }  else {
                    binding?.textInputLayoutTitle?.error = null
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        binding?.buttonSaveTask?.setOnClickListener {
            showSaveConfirmationDialog()
        }
    }

    private fun getTaskDetails() {
        val call: Call<ApiResponse>? = taskId?.let { getSingleTaskApiInterface!!.getByTaskId(it) }
        call?.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val task = response.body()?.task
                    if (task != null) {
                        binding?.textInputEditTextTitle?.setText(task.title)
                        binding?.textInputEditTextDescription?.setText(task.description)
                        binding?.textInputEditTextStartDate?.setText(
                            task.startDate?.let { dateFormater.formatDate(it) } ?: "N/A"
                        )
                        binding?.textInputEditTextDueDate?.setText(
                            task.endDate?.let { dateFormater.formatDate(it) } ?: "N/A"
                        )
                        updateDropdownStatus(task.status)
                    } else {
                        context?.let { ToastUtil.showCustomToast(it, getString(R.string.retry_to_get_task_toast), false) }
                    }
                } else {
                    context?.let { ToastUtil.showCustomToast(it, getString(R.string.retry_to_get_task_toast), false) }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                context?.let { ToastUtil.showCustomToast(it, getString(R.string.retry_to_get_task_toast), false) }
            }
        })
    }

    private fun updateDropdownStatus(currentStatus: String?) {
        if (currentStatus != null) {
            binding?.statusDropdown?.setText(currentStatus, false)
            val statusColor = when (currentStatus) {
                "To Do" -> Color.GRAY
                "Completed" -> Color.GREEN
                "On Hold" -> Color.RED
                "In Progress" -> context?.resources?.getColor(R.color.dark_yellow, context?.theme)
                else -> Color.TRANSPARENT
            }
            binding?.statusDropdown?.setTextColor(statusColor ?: Color.TRANSPARENT)
        }
    }

    private fun editTask() {
        val title = binding?.textInputEditTextTitle?.text.toString().trim()
        val description = binding?.textInputEditTextDescription?.text.toString().trim()
        val dueDate = binding?.textInputEditTextDueDate?.text.toString().trim()
        val status = binding?.statusDropdown?.text.toString().trim()
        var isValid = true

        if (title.isEmpty()) {
            binding?.textInputLayoutTitle?.error = getString(R.string.Title_is_required_text)
            binding?.textInputEditTextTitle?.requestFocus()
            isValid = false
        }  else {
            binding?.textInputLayoutTitle?.error = null
        }

        if (description.isEmpty()) {
            binding?.textInputEditTextDescription?.error = getString(R.string.description_is_required_text)
            binding?.textInputEditTextDescription?.requestFocus()
            isValid = false
        } else {
            binding?.textInputEditTextDescription?.error = null
        }

        if (dueDate.isEmpty()) {
            binding?.textInputLayoutDueDate?.error = getString(R.string.due_date_is_required_text)
            binding?.textInputEditTextDueDate?.requestFocus()
            isValid = false
        } else {
            binding?.textInputLayoutDueDate?.error = null
        }

        if (status.isEmpty()) {
            binding?.textInputLayoutStatus?.error = getString(R.string.status_is_required_text)
            binding?.statusDropdown?.requestFocus()
            isValid = false
        } else {
            binding?.textInputLayoutStatus?.error = null
        }

        if (!isValid) {
            return
        }

        val formattedDate = formatToIsoDate(dueDate)
        val editTaskRequest = taskId?.let { EditTaskRequest(it, title, description, status, formattedDate) }

        val call = editTaskRequest?.let { editTaskApiInterface?.editTask(it) }

        call?.enqueue(object : Callback<EditTaskResponse> {
            override fun onResponse(
                call: Call<EditTaskResponse>,
                response: Response<EditTaskResponse>
            ) {
                if (response.isSuccessful) {
                    context?.let {
                        ToastUtil.showCustomToast(it, getString(R.string.task_updated_successfully_toast), true)
                        findNavController().popBackStack()
                    }
                } else {
                    context?.let { ToastUtil.showCustomToast(it, getString(R.string.retry_to_update_task_toast), false) }
                }
            }

            override fun onFailure(call: Call<EditTaskResponse>, t: Throwable) {
                context?.let { ToastUtil.showCustomToast(it, getString(R.string.retry_to_update_task_toast), false) }
            }
        })
    }

    private fun showSaveConfirmationDialog() {
        val dialogBinding = DialogSaveTaskBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.buttonYes.setOnClickListener {
            editTask()
            dialog.dismiss()
        }

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDueDate = calendar.time

                val selectedDate =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDueDate)
                binding?.textInputEditTextDueDate?.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun formatToIsoDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

}



