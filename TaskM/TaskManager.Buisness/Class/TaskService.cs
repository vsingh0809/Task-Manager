
#region namespace
using AutoMapper;
using Microsoft.Extensions.Logging;
using TaskManager.Buisness.Interface;
using TaskManager.DataAccess.Interface;
using TaskManager.Entities.TaskDTO;
using TaskManager.Models.Models;
#endregion

namespace TaskManager.Buisness.Class
{
    public class TaskService : ITaskService
    {
        private readonly ITaskRepository _taskRepository;
        private readonly IMapper _mapper;
        private readonly ILogger<TaskService> _logger;

        public TaskService(ITaskRepository taskRepository, IMapper mapper, ILogger<TaskService> logger)
        {
            _taskRepository = taskRepository;
            _mapper = mapper;
            _logger = logger;
        }

        #region Save task by V
        public async Task<TaskResponseDTO> SaveTaskAsync(SaveTaskRequestDTO TaskResponseDTO, int userId)
        {
            _logger.LogInformation("SaveTaskAsync called for user {UserId} ", userId);

            var task = _mapper.Map<Tasks>(TaskResponseDTO);
            var taskfromdb = await _taskRepository.SaveTaskAsync(task, userId);
            var result = _mapper.Map<TaskResponseDTO>(taskfromdb);
            if (result != null)
            {
                _logger.LogInformation("Task successfully saved for user {UserId}", userId);
            }
            else
            {
                _logger.LogWarning("Failed to save task for user {UserId}.", userId);
            }

            return result;
        }
        #endregion

        #region Get task details
        public async Task<TaskResponseDTO> GetTaskByIdAsync(int taskId, int userId)
        {
            _logger.LogInformation("GetTaskByIdAsync called for TaskId: {TaskId} and UserId: {UserId}", taskId, userId);

            var task = await _taskRepository.GetTaskByIdAsync(taskId, userId);

            if (task != null)
            {
                _logger.LogInformation("Task {TaskId} found for user {UserId}", taskId, userId);
                return _mapper.Map<TaskResponseDTO>(task);
            }

            _logger.LogWarning("Task {TaskId} not found for user {UserId}", taskId, userId);
            return null;
        }
        #endregion

        #region Get all tasks in app
        public async Task<IEnumerable<TaskResponseDTO>> GetAllTasksAsync()
        {
            _logger.LogInformation("GetAllTasksAsync called");

            var tasks = await _taskRepository.GetAllTasksAsync();

            _logger.LogInformation("Found {TaskCount} tasks in the application", tasks.Count());
            return _mapper.Map<IEnumerable<TaskResponseDTO>>(tasks);
        }
        #endregion

        #region Get all tasks of user
        public async Task<IEnumerable<TaskResponseDTO>> GetAllTasksByUserIdAsync(int userId)
        {
            _logger.LogInformation("GetAllTasksByUserIdAsync called for UserId: {UserId}", userId);

            var tasks = await _taskRepository.GetAllTasksByUserIdAsync(userId);

            _logger.LogInformation("Found {TaskCount} tasks for UserId: {UserId}", tasks.Count(), userId);
            return _mapper.Map<IEnumerable<TaskResponseDTO>>(tasks);
        }
        #endregion

        #region Edit tasks
        public async Task<bool> EditTaskAsync(EditTaskRequestDTO TaskResponseDTO, int userId)
        {
            _logger.LogInformation("EditTaskAsync called for UserId: {UserId} and TaskId: {TaskId}", userId, TaskResponseDTO?.TaskId);

            var result = await _taskRepository.EditTaskAsync(TaskResponseDTO, userId);

            if (result)
            {
                _logger.LogInformation("Task {TaskId} successfully edited by UserId: {UserId}", TaskResponseDTO?.TaskId, userId);
            }
            else
            {
                _logger.LogWarning("Failed to edit Task {TaskId} by UserId: {UserId}", TaskResponseDTO?.TaskId, userId);
            }

            return result;
        }
        #endregion

        #region Soft delete tasks
        public async Task<bool> SoftDeleteTaskAsync(int taskId, int userId)
        {
            _logger.LogInformation("SoftDeleteTaskAsync called for TaskId: {TaskId} and UserId: {UserId}", taskId, userId);

            var result = await _taskRepository.SoftDeleteTaskAsync(taskId, userId);

            if (result)
            {
                _logger.LogInformation("Task {TaskId} successfully soft deleted by UserId: {UserId}", taskId, userId);
            }
            else
            {
                _logger.LogWarning("Failed to soft delete Task {TaskId} by UserId: {UserId}", taskId, userId);
            }

            return result;
        }
        #endregion

        #region Multitple tasks delete
        public async Task<bool> MultiselectSoftDeleteAsync(List<int> taskIds, int userId)
        {
            _logger.LogInformation("MultiselectSoftDeleteAsync called for UserId: {UserId} with TaskIds: {TaskIds}", userId, string.Join(",", taskIds));

            var result = await _taskRepository.MultiselectSoftDeleteTasksAsync(taskIds, userId);

            if (result)
            {
                _logger.LogInformation("{TaskCount} tasks successfully soft deleted by UserId: {UserId}", taskIds.Count, userId);
            }
            else
            {
                _logger.LogWarning("Failed to soft delete tasks for UserId: {UserId}. TaskIds: {TaskIds}", userId, string.Join(",", taskIds));
            }

            return result;
        }
        #endregion

        #region Get tasks of user by start date
        public async Task<IEnumerable<TaskResponseDTO>> GetTasksByStartDateAsync(DateTime startDate, int userId)
        {
            var tasks = await _taskRepository.GetTasksByStartDateAsync(startDate, userId);
            return _mapper.Map<IEnumerable<TaskResponseDTO>>(tasks);
        }
        #endregion

        #region Get tasks of user by end date
        public async Task<IEnumerable<TaskResponseDTO>> GetTasksByEndDateAsync(DateTime endDate, int userId)
        {
            var tasks = await _taskRepository.GetTasksByEndDateAsync(endDate, userId);
            return _mapper.Map<IEnumerable<TaskResponseDTO>>(tasks);
        }
        #endregion

        #region Get tasks by date range
        public async Task<IEnumerable<TaskResponseDTO>> GetTasksByDateRangeAsync(DateTime startDate, DateTime endDate, int userId)
        {
            var tasks = await _taskRepository.GetTasksByDateRangeAsync(startDate, endDate, userId);
            return _mapper.Map<IEnumerable<TaskResponseDTO>>(tasks);
        }
        #endregion


        //NN



        #region Get task of user by title
        public async Task<IEnumerable<TaskResponseDTO>> GetTasksByTitleAsync(string title, int userid)
        {
            var tasks = await _taskRepository.GetTasksByTitleAsync(title, userid);
            return _mapper.Map<IEnumerable<TaskResponseDTO>>(tasks);
        }
        #endregion

        

        #region Delete task
        public async Task<bool> DeleteTaskAsync(int taskId)
        {
            return await _taskRepository.DeleteTaskAsync(taskId);
        }
        #endregion


    }

}

