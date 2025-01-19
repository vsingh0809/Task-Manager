package com.SKO.Taskmanager.fragments
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.SKO.Taskmanager.R
import com.SKO.Taskmanager.classes.ApiClient
import com.SKO.Taskmanager.databinding.DialogDeleteBinding
import com.SKO.Taskmanager.databinding.DialogLogoutBinding
import com.SKO.Taskmanager.databinding.FragmentViewTaskBinding
import com.SKO.Taskmanager.interfaces.DeleteSingleTaskApiInterface
import com.SKO.Taskmanager.interfaces.GetSingleTaskApiInterface
import com.SKO.Taskmanager.interfaces.TaskApiInterface
import com.SKO.Taskmanager.responses.ApiResponse
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.utils.DateFormater
import com.SKO.Taskmanager.utils.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ViewTaskFragment : Fragment() {
    private var taskId: Int? = null
    private var binding: FragmentViewTaskBinding? = null
    //private lateinit var taskApiInterface: TaskApiInterface
    private var getSingleTaskApiInterface : GetSingleTaskApiInterface?= null
    private var deleteSingleTaskApiInterface : DeleteSingleTaskApiInterface?= null

    private var toolbar : androidx.appcompat.widget.Toolbar ?= null


    var dateFormater = DateFormater()

    val args: ViewTaskFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout
        binding = FragmentViewTaskBinding.inflate(inflater, container, false)

        toolbar = binding?.toolbarInclude?.toolbar

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        binding?.progressBarViewBinding?.visibility = View.GONE

        // Use binding.root to get the root view
        val rootView = binding!!.root
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize the API interface
        getSingleTaskApiInterface =
            ApiClient.getMainClient(requireActivity()).create(GetSingleTaskApiInterface::class.java)

        deleteSingleTaskApiInterface = ApiClient.getMainClient(requireActivity()).create(DeleteSingleTaskApiInterface::class.java)

        // Retrieve task ID passed via Bundle
        taskId = args.taskid
        // Fetch task details from the API
        getTaskDetails()

        // Set button actions
        binding!!.editButtonTaskView.setOnClickListener { openEditTaskFragment(taskId!!) }

        binding!!.deleteButtonTaskView.setOnClickListener {
            binding!!.deleteButtonTaskView.isEnabled = false
            deleteTaskDialog(taskId!!) }
    }

    private fun deleteTaskDialog(taskId: Int) {
        val dialogBinding = DialogDeleteBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false) // Prevent the dialog from being dismissed by tapping outside
            .create()


        dialogBinding.buttonYes.setOnClickListener {
            deleteTask(taskId)
            dialog.dismiss()
        }

        dialogBinding.buttonCancel.setOnClickListener {
            binding!!.deleteButtonTaskView.isEnabled = true
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun deleteTask(taskId: Int) {
        binding?.progressBarViewBinding?.visibility = View.VISIBLE

        val call : Call<StatusResponse> ? = deleteSingleTaskApiInterface?.deleteTask(taskId)
        call?.enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                binding?.progressBarViewBinding?.visibility = View.GONE

                if(response.isSuccessful){
                    ToastUtil.showCustomToast(requireContext(), "${response.body()!!.message}", true )

                    val action = ViewTaskFragmentDirections.actionViewTaskFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
                else{
                    ToastUtil.showCustomToast(requireContext(), getString(R.string.failed_to_delete_task), false)
                }
            }

            override fun onFailure(call: Call<StatusResponse>, response: Throwable) {
                binding?.progressBarViewBinding?.visibility = View.GONE

                ToastUtil.showCustomToast(requireContext(), getString(R.string.some_error_occured), false)
            }

        })

    }

    //
    private fun getTaskDetails() {
        val call: Call<ApiResponse>? = taskId?.let { getSingleTaskApiInterface?.getByTaskId(it) }
        call?.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val task = apiResponse?.task

                    if (task != null) {
                        binding?.textViewViewTaskTitle?.text = task.title
                        binding?.teskDiscriptionTextView?.text = task.description
                        binding?.creationDateViewTaskDate?.text =
                            task.startDate?.let { dateFormater.formatDate(it) } ?: "N/A"
                        binding?.endDateViewTaskDate?.text =
                            task.endDate?.let { dateFormater.formatDate(it) } ?: "N/A"
                        binding?.statusTaskTextViewtoshow?.text = task.status
                        when (task.status) {
                            "To Do" -> binding?.statusTaskTextViewtoshow?.setTextColor(Color.GRAY)
                            "Completed" -> binding?.statusTaskTextViewtoshow?.setTextColor(Color.GREEN)
                            "On Hold" -> binding?.statusTaskTextViewtoshow?.setTextColor(Color.RED)
                            "In Progress" -> binding?.statusTaskTextViewtoshow?.setTextColor(context!!.resources.getColor(R.color.dark_yellow, context!!.theme))
                            else -> binding?.statusTaskTextViewtoshow?.setTextColor(Color.TRANSPARENT)
                        }
                    } else {
//                        Toast.makeText(context, "Task details not found.", Toast.LENGTH_SHORT).show()
                        ToastUtil.showCustomToast(requireContext(), getString(R.string.task_details_not_found_toast), false)
                    }
                } else {
//                    Toast.makeText(context, "Failed to fetch task details.", Toast.LENGTH_SHORT).show()
                    ToastUtil.showCustomToast(requireContext(), getString(R.string.failed_to_fetch_task_details_toast), false)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, getString(R.string.network_request_failed), Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun openEditTaskFragment(taskId: Int) {
     val action=  ViewTaskFragmentDirections.actionViewTaskFragmentToEditTaskFragment(taskId)
        Log.d("tskid",taskId.toString())
        findNavController().navigate(action)
    }


}
