

#region namespace
using TaskManager.Entities.TaskDTO;
using TaskManager.Models.Models;
#endregion

namespace TaskManager.DataAccess.Interface
{
    public interface ITaskRepository
    {
        Task<Tasks> SaveTaskAsync(Tasks task, int userId);
        Task<Tasks> GetTaskByIdAsync(int taskId, int userId);
        Task<IEnumerable<Tasks>> GetAllTasksAsync();
        Task<IEnumerable<Tasks>> GetAllTasksByUserIdAsync(int userId);
        Task<IEnumerable<Tasks>> GetTasksByStartDateAsync(DateTime startDate, int userId);
        Task<IEnumerable<Tasks>> GetTasksByEndDateAsync(DateTime endDate, int userId);
        Task<IEnumerable<Tasks>> GetTasksByTitleAsync(string title, int userId);
        Task<IEnumerable<Tasks>> GetTasksByDateRangeAsync(DateTime startDate, DateTime endDate, int userId);
        Task<bool> EditTaskAsync(EditTaskRequestDTO editTask, int userId);
        Task<bool> DeleteTaskAsync(int taskId);
        Task<bool> SoftDeleteTaskAsync(int taskId, int userId);
        Task<bool> MultiselectSoftDeleteTasksAsync(List<int> taskIds, int userID);

    }
}

