
#region namsespace

using TaskManager.Entities.TaskDTO;
using TaskManager.Models.Models;
#endregion

namespace TaskManager.Buisness.Interface
{
    public interface ITaskService
    {
        Task<TaskResponseDTO> SaveTaskAsync(SaveTaskRequestDTO TaskResponseDTO, int userId);
        Task<TaskResponseDTO> GetTaskByIdAsync(int taskId, int userId);
        Task<IEnumerable<TaskResponseDTO>> GetAllTasksAsync();
        Task<IEnumerable<TaskResponseDTO>> GetAllTasksByUserIdAsync(int userId);
        Task<IEnumerable<TaskResponseDTO>> GetTasksByStartDateAsync(DateTime startDate, int userId);
        Task<IEnumerable<TaskResponseDTO>> GetTasksByEndDateAsync(DateTime endDate, int userId);
        Task<IEnumerable<TaskResponseDTO>> GetTasksByTitleAsync(string title, int userid);
        Task<IEnumerable<TaskResponseDTO>> GetTasksByDateRangeAsync(DateTime startDate, DateTime endDate, int userId);
        Task<bool> EditTaskAsync(EditTaskRequestDTO TaskResponseDTO, int userID);
        Task<bool> DeleteTaskAsync(int taskId);
        Task<bool> SoftDeleteTaskAsync(int taskId, int userId);
        Task<bool> MultiselectSoftDeleteAsync(List<int> taskIds, int userId);

    }
}


