package com.SKO.Taskmanager.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.adapter.TasksAdapter
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.databinding.FragmentHomeBinding
import com.SKO.Taskmanager.interfaces.DeleteMultipleTasksInterface
import com.SKO.Taskmanager.interfaces.GetAllTasksApiInterface
import com.SKO.Taskmanager.interfaces.TaskApiInterface
import com.SKO.Taskmanager.responses.GetAllTaskResponse
import com.SKO.Taskmanager.responses.MultiselectDeleteRequest
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.responses.TaskDataClass
import com.SKO.Taskmanager.utils.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.Calendar

class HomeFragment : Fragment(), View.OnLongClickListener {
    private var _binding: FragmentHomeBinding ? = null
    private val binding get() = _binding

    private var recyclerViewAdapter: TasksAdapter ?= null
    private var jwtToken: String ?= null
    //private var taskApiInterface: TaskApiInterface ?= null
    private var getAllTasksApiInterface : GetAllTasksApiInterface?= null
    private var deleteMultipleTasksInterface : DeleteMultipleTasksInterface?= null

    private var mainTaskList = ArrayList<TaskDataClass>()
    var multiDeleteList = mutableListOf<TaskDataClass>()
    private var originalTaskList = ArrayList<TaskDataClass>()

    private var fromLocalDateTime: LocalDateTime? = null
    private var toLocalDateTime: LocalDateTime? = null

    private var progressDialog:ProgressDialog?=null

    //private var progressBar : ProgressBar ? = null

    private var toolbar : androidx.appcompat.widget.Toolbar ?= null

    var isContexualModeEnabled = false

    private var itemCounter  : TextView ?= null

    private var counter = 0

    private var isInitialLoad = true


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ConstraintLayout? {
        showProgressDialog()

        // Simulate a delay for fetching user data
        Handler(Looper.getMainLooper()).postDelayed({
            dismissProgressDialog()
            // Proceed with further logic, e.g., navigate to the next activity
        }, 1000)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding?.progressBarHomeFragment?.visibility = View.GONE

        setHasOptionsMenu(true)
        toolbar = binding?.toolbarInclude?.toolbar
        itemCounter = binding?.toolbarInclude?.itemCounter
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        initializeApiClient()
        setupRecyclerView()
        setupUIListeners()
        setupStartDateTextListener()
        setupEndDateTextListener()
        fetchInitialTasks()
       return binding?.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)  {
        inflater.inflate(R.menu.normal_menu, menu)
    }

    private fun initializeApiClient() {
            getAllTasksApiInterface = ApiClient.getMainClient(requireContext()).create(GetAllTasksApiInterface::class.java)
        deleteMultipleTasksInterface = ApiClient.getMainClient(requireContext()).create(com.SKO.Taskmanager.interfaces.DeleteMultipleTasksInterface::class.java)
        jwtToken = requireContext().getSharedPreferences(getString(R.string.shared_preference_variable_saveData), Context.MODE_PRIVATE)
            .getString(getString(R.string.token), null) ?: ""
    }

    override fun onResume() {
        super.onResume()
//        binding?.editTextHomeFragmentsearchEditText?.text?.clear()
        isContexualModeEnabled=false
    }

    override fun onPause() {
        super.onPause()
        isContexualModeEnabled=false
        removeContexualActionMode()
        binding?.editTextHomeFragmentsearchEditText?.text?.clear()
    }

    @SuppressLint("RestrictedApi")
    private fun setupRecyclerView() {
        recyclerViewAdapter = TasksAdapter(ArrayList(), this, requireContext(),
            onItemClickListener = {
                showProgressDialog()

                // Simulate a delay for fetching user data
                Handler(Looper.getMainLooper()).postDelayed({
                    dismissProgressDialog()
                    // Proceed with further logic, e.g., navigate to the next activity
                }, 1000)

                val action = HomeFragmentDirections.actionHomeFragmentToViewTaskFragment(it.taskId)
                Log.d("sill",it.taskId.toString())
                findNavController().navigate(action)},
            onEditClickListener = { task -> navigateToTaskActivity(task.taskId) })
        binding?.recyclerViewHomeFragment?.layoutManager = LinearLayoutManager(requireContext())
        binding?.recyclerViewHomeFragment?.adapter = recyclerViewAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUIListeners() {
        binding?.addTaskFloatingButton?.setOnClickListener { navigateToAddTask() }
        setupSearchListener()

        // Set listeners for the calendar icons to open DatePickerDialog
        binding?.startDateCalanderIcon?.setOnClickListener { showDatePickerDialog(true) }
        binding?.endDateCalanderIcon?.setOnClickListener { showDatePickerDialog(false) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupStartDateTextListener() {
        binding?.startDateText?.setOnClickListener {
            fromLocalDateTime = null
            binding?.startDateText?.text = getString(R.string.start_date_text)
            filterTasksByDate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupEndDateTextListener() {
        binding?.endDateText?.setOnClickListener {
            toLocalDateTime = null
            binding?.endDateText?.text = getString(R.string.end_date_text)
            filterTasksByDate()
        }
    }

    private fun setupSearchListener() {
        binding?.editTextHomeFragmentsearchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearDates()
                filterTasks(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val selectedDate = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDayOfMonth, 0, 0)
            if (isStartDate) {
                fromLocalDateTime = selectedDate
                binding?.startDateText?.text = selectedDate.toLocalDate().toString()
                if (toLocalDateTime != null && fromLocalDateTime?.isAfter(toLocalDateTime) == true) {
                    ToastUtil.showCustomToast(requireContext(),getString(R.string.please_enter_a_valid_start_date_toast),false)
                    fromLocalDateTime = null
                    binding?.startDateText?.text = getString(R.string.start_date_text)
                    // Clear the task list or show a message if needed
                    recyclerViewAdapter?.setTasks(emptyList())
                    updateRecyclerViewVisibility()
                } else {
                    filterTasksByDate()
                }
            } else {
                toLocalDateTime = selectedDate
                binding?.endDateText?.text = selectedDate.toLocalDate().toString()
                // Check if the end date is before the start date
                if (fromLocalDateTime != null && toLocalDateTime?.isBefore(fromLocalDateTime) == true) {
                    ToastUtil.showCustomToast(requireContext(),getString(R.string.please_enter_a_valid_end_date_toast),false)
                    toLocalDateTime = null
                    binding?.endDateText?.text = getString(R.string.end_date_text)
                    // Clear the task list or show a message if needed
                    recyclerViewAdapter?.setTasks(emptyList())
                    updateRecyclerViewVisibility()
                } else {
                    filterTasksByDate()
                }
            }
        }, year, month, day)

        datePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchInitialTasks() {
        getAllTasksByToken()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAllTasksByToken() {
        val call: Call<GetAllTaskResponse>? = getAllTasksApiInterface?.getAllTasksByToken()
        call?.enqueue(object : Callback<GetAllTaskResponse> {
            override fun onResponse(call: Call<GetAllTaskResponse>, response: Response<GetAllTaskResponse>) {
                if (response.isSuccessful) {
                    if(response.body()?.meassage == getString(R.string.success_responce_text)){
                        val taskList = response.body()?.task
                        taskList?.let {
                            mainTaskList.clear()
                            mainTaskList.addAll(it)
                            originalTaskList.clear()
                            originalTaskList.addAll(it)
                            recyclerViewAdapter?.setTasks(mainTaskList)
                            updateRecyclerViewVisibility()
                        }
                    } else {
//                        updateRecyclerViewVisibility()
//                        Toast.makeText(context, "${response.body()?.message}", Toast.LENGTH_SHORT).show()
//                        ToastUtil.showCustomToast(requireContext(), "${response.body()?.message}", false)
                        recyclerViewAdapter?.setTasks(emptyList())
                        updateRecyclerViewVisibility()

                    }
                } else {
//                    Toast.makeText(context, "Some error occurred in calling the API", Toast.LENGTH_SHORT).show()
                    ToastUtil.showCustomToast(requireContext(), getString(R.string.some_error_occurred_in_calling_the_API_toast),false)
                }
                isInitialLoad = false
            }

            override fun onFailure(call: Call<GetAllTaskResponse>, t: Throwable) {
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.d("In_main_activity2", "${t.message}")
                isInitialLoad = false
            }
        })
    }

    private fun filterTasks(query: String) {
        val filteredList = if (query.isNotEmpty()) {
            originalTaskList.filter { task ->
                task.taskId.toString().contains(query, ignoreCase = true) ||
                        task.title.contains(query, ignoreCase = true)
            }
        } else {
            originalTaskList
        }
        recyclerViewAdapter?.setTasks(filteredList)
        updateRecyclerViewVisibility()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterTasksByDate() {
        val filteredList = when {
            fromLocalDateTime != null && toLocalDateTime == null -> filterByStartDate()
            fromLocalDateTime == null && toLocalDateTime != null -> filterByEndDate()
            fromLocalDateTime != null && toLocalDateTime != null -> filterByDateRange()
            else -> originalTaskList
        }
        recyclerViewAdapter?.setTasks(filteredList)
        updateRecyclerViewVisibility()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterByStartDate(): List<TaskDataClass> {
        return originalTaskList.filter { task ->
            val taskStartDate = LocalDateTime.parse(task.startDate?.substring(0, 19))
            taskStartDate.toLocalDate() == fromLocalDateTime?.toLocalDate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterByEndDate(): List<TaskDataClass> {
        return originalTaskList.filter { task ->
            val taskEndDate = LocalDateTime.parse(task.endDate?.substring(0, 19))
            taskEndDate.toLocalDate() == toLocalDateTime?.toLocalDate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterByDateRange(): List<TaskDataClass> {
        return originalTaskList.filter { task ->
            val taskEndDate = LocalDateTime.parse(task.endDate?.substring(0, 19))
            taskEndDate.toLocalDate() >= fromLocalDateTime?.toLocalDate()
                    && taskEndDate.toLocalDate() <= toLocalDateTime?.toLocalDate()
        }.sortedBy { LocalDateTime.parse(it.endDate?.substring(0, 19)) }
    }

//    private fun updateRecyclerViewVisibility() {
//        val noResultsTextView = binding?.textViewNoResultsTextView
//        if (recyclerViewAdapter?.getTasks()?.isEmpty() == true) {
//            binding?.recyclerViewHomeFragment?.visibility = View.GONE
//            noResultsTextView?.visibility = View.VISIBLE
//            noResultsTextView?.text = getString(R.string.no_tasks_found_text)
//            if (!isInitialLoad && (binding?.editTextHomeFragmentsearchEditText?.text?.isNotEmpty() == true)) {
//                ToastUtil.showCustomToast(requireContext(), getString(R.string.no_search_result_found), false)
//            }
//        } else {
//            binding?.recyclerViewHomeFragment?.visibility = View.VISIBLE
//            noResultsTextView?.visibility = View.GONE
//        }
//    }

    private fun updateRecyclerViewVisibility() {
        val noResultsTextView = binding?.textViewNoResultsTextView
        val isRecyclerViewEmpty = recyclerViewAdapter?.itemCount == 0

        binding?.recyclerViewHomeFragment?.visibility = if (isRecyclerViewEmpty) View.GONE else View.VISIBLE
        noResultsTextView?.visibility = if (isRecyclerViewEmpty) View.VISIBLE else View.GONE

        if (isRecyclerViewEmpty) {
            noResultsTextView?.text = getString(R.string.no_tasks_found_text)
            if (!isInitialLoad && (binding?.editTextHomeFragmentsearchEditText?.text?.isNotEmpty() == true)) {
                ToastUtil.showCustomToast(requireContext(), getString(R.string.no_search_result_found), false)
            }
        }
    }

    private fun clearDates() {
        fromLocalDateTime = null
        toLocalDateTime = null
        binding?.startDateText?.text = getString(R.string.start_date_text)
        binding?.endDateText?.text = getString(R.string.end_date_text)
    }

    private fun navigateToTaskActivity(taskId: Int) {

    }

    private fun navigateToAddTask() {
        findNavController().navigate(R.id.action_homeFragment_to_addTaskFragment)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.fetching_user_tasks_text))
            setCancelable(false)
            show()
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLongClick(view: View?): Boolean {
        isContexualModeEnabled = true
        binding?.addTaskFloatingButton?.isEnabled = false
        binding?.addTaskFloatingButton?.visibility = View.GONE

        recyclerViewAdapter?.disableItemListeners(true)

        toolbar?.menu?.clear()
        toolbar?.title=""
        toolbar?.inflateMenu(R.menu.contexual_menu)
        //updateDeleteIconVisibility()

        recyclerViewAdapter?.notifyDataSetChanged()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return true
    }

    private fun updateDeleteIconVisibility() {
        val deleteItem = toolbar?.menu?.findItem(R.id.delete)
        deleteItem?.isEnabled = multiDeleteList.isNotEmpty()
    }

    fun makeSelection(v: View, adapterPosition: Int) {
        val task = mainTaskList[adapterPosition]

        if ((v as CheckBox).isChecked) {
            multiDeleteList.add(task)
            counter++
        } else {
            multiDeleteList.remove(task)
            counter--
        }
        updateCounter()
        //updateDeleteIconVisibility()
    }

     fun updateCounter() {
        itemCounter?.text = "$counter Items selected"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_all -> {
                selectAllTasks()
            }
            R.id.delete -> {
                if (multiDeleteList.isEmpty()) {
                    // Show a warning toast or dialog if no tasks are selected for deletion
                    ToastUtil.showCustomToast(requireContext(), getString(R.string.no_tasks_selected_for_deletion_toast), false)
                } else {
                    val copiedList = ArrayList(multiDeleteList)
                    showDeleteConfirmationDialog(copiedList)
                    removeContexualActionMode()
                }
            }
            android.R.id.home -> {
                removeContexualActionMode()
                recyclerViewAdapter?.notifyDataSetChanged()
                Log.d("ContextualMode", "Item Selected: ${item.itemId}")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectAllTasks() {

        multiDeleteList.clear()
        multiDeleteList.addAll(mainTaskList)

        counter = multiDeleteList.size
        updateCounter()

        // Notify the adapter to reflect the changes in the checkboxes
        recyclerViewAdapter?.notifyDataSetChanged()

        //updateDeleteIconVisibility()
    }

    private fun showDeleteConfirmationDialog(multiDeleteList: ArrayList<TaskDataClass>) {

        val dialogBinding = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding)
            .setCancelable(false)
            .create()

        val yesButton = dialogBinding.findViewById<TextView>(R.id.buttonYes)
        val noButton = dialogBinding.findViewById<TextView>(R.id.buttonCancel)
        Log.d("home fragment -1", "${multiDeleteList.size}")

        val copiedList = ArrayList(multiDeleteList)
        Log.d("home fragment -1", "Before Dialog - Size: ${copiedList.size}")

        yesButton.setOnClickListener {
            Log.d("home fragment -2", "After Yes - Size: ${copiedList.size}")

            if (copiedList.isNotEmpty()) {
                removeTasks(copiedList)
            } else {
                ToastUtil.showCustomToast(requireContext(), getString(R.string.no_tasks_selected_for_deletion_toast), false)
            }
            dialog.dismiss()
        }

        noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun removeTasks(deleteTaskList: ArrayList<TaskDataClass>) {

        binding?.progressBarHomeFragment?.visibility = View.VISIBLE

        val taskIdsToDelete = deleteTaskList.map { it.taskId }

        val multiselectDeleteRequest = MultiselectDeleteRequest(taskIdsToDelete)

        val call: Call<StatusResponse>? = deleteMultipleTasksInterface?.deleteMultipleTasks(multiselectDeleteRequest)

        call?.enqueue(object : Callback<StatusResponse>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                binding?.progressBarHomeFragment?.visibility = View.GONE
                if(response.isSuccessful){
                    ToastUtil.showCustomToast(requireContext(), "${response.body()?.message}", true  )
                    recyclerViewAdapter?.notifyDataSetChanged()
                    mainTaskList.clear()
                    originalTaskList.clear()
                    recyclerViewAdapter?.setTasks(mainTaskList)
                    updateRecyclerViewVisibility()
                    getAllTasksByToken()
                }
                else{
                    ToastUtil.showCustomToast(requireContext(), getString(R.string.failed_to_delete_task), false)
                    recyclerViewAdapter?.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<StatusResponse>, response: Throwable) {
                binding?.progressBarHomeFragment?.visibility = View.GONE
                ToastUtil.showCustomToast(requireContext(), getString(R.string.network_error_occured_toast), false)
            }

        })

    }

    private fun removeContexualActionMode() {
        isContexualModeEnabled = false
        binding?.addTaskFloatingButton?.isEnabled = true
        binding?.addTaskFloatingButton?.visibility = View.VISIBLE

        // Restore Item Click and Edit Click Listeners
        recyclerViewAdapter?.disableItemListeners(false)
        itemCounter?.text = getString(R.string.app_name)
        toolbar?.menu?.clear()
        toolbar?.inflateMenu(R.menu.normal_menu)
        counter = 0
        multiDeleteList.clear()
        recyclerViewAdapter?.notifyDataSetChanged()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


}