
#region namespace
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using MySql.Data.MySqlClient;
using MySql.EntityFrameworkCore.Extensions;
using Org.BouncyCastle.Pqc.Crypto.Lms;
using TaskManager.DataAccess.Interface;
using TaskManager.Entities.TaskDTO;
using TaskManager.Models.Data;
using TaskManager.Models.Models;
#endregion


namespace TaskManager.DataAccess.Class
{
    public class TasksRepository : ITaskRepository
    {
        private readonly MyDbContext _context;
        private readonly ILogger<TasksRepository> _logger;
        public TasksRepository(MyDbContext context, ILogger<TasksRepository> logger)
        {
            _context = context;
            _logger = logger;
        }


        #region Save task:Rework by vaibhav

        public async Task<Tasks> SaveTaskAsync(Tasks tasks, int userId)
        {
            try
            {
                var user = await _context.Users
                .Where(u => u.UserId == userId)
                          .FirstOrDefaultAsync();

                tasks.CreatedBy = user.Username;
                tasks.LastUpdatedBy = user.Username;
                tasks.UserId = userId;
                tasks.Status = "To Do";
                _context.Tasks.Add(tasks);
                await _context.SaveChangesAsync();
                // return true;
                return tasks;
            }
            catch (Exception ex)
            {
                throw new Exception("Failed to save task.", ex);
            }
        }
        #endregion

        #region Get task details by id

        public async Task<Tasks> GetTaskByIdAsync(int taskId, int userId)
        {
            return await _context.Tasks.FirstOrDefaultAsync(t => t.TaskId == taskId && t.UserId == userId);
        }
        #endregion

        #region Get all tasks of user
        public async Task<IEnumerable<Tasks>> GetAllTasksByUserIdAsync(int userId)
        {
            // Most recent Taskss first
            return await _context.Tasks
                                 .Where(t => t.UserId == userId && t.EnabledFlag != "N")
                                 .OrderBy(t => t.EndDate)
                                 .ToListAsync();
        }
        #endregion

        #region Get all tasks in app
        async Task<IEnumerable<Tasks>> ITaskRepository.GetAllTasksAsync()
        {
            return await _context.Tasks.ToListAsync();
        }
        #endregion

        #region Edit task:re
        async Task<bool> ITaskRepository.EditTaskAsync(EditTaskRequestDTO editTasksDto, int userId)
        {
            try
            {
                var Tasks = await _context.Tasks
                    .FirstOrDefaultAsync(t => t.TaskId == editTasksDto.TaskId);

                var user = await _context.Users.FirstOrDefaultAsync(t => t.UserId == userId);

                if (Tasks == null)
                {
                    return false;
                }

                Tasks.UserId = userId;

                if (!string.IsNullOrEmpty(editTasksDto.Title))
                {
                    Tasks.Title = editTasksDto.Title;
                }

                if (!string.IsNullOrEmpty(editTasksDto.Description))
                {
                    Tasks.Description = editTasksDto.Description;
                }

                if (!string.IsNullOrEmpty(editTasksDto.Status))
                {
                    Tasks.Status = editTasksDto.Status;
                }

                if (editTasksDto.EndDate != default)
                {
                    Tasks.EndDate = editTasksDto.EndDate;
                }
                Tasks.LastUpdatedBy = user.Username;
                Tasks.LastUpdationDate = DateTime.UtcNow;

                await _context.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                // Log.Error("Error occured while ")
                return false;
            }

        }
        #endregion

        #region Get tasks of user by start date
        public async Task<IEnumerable<Tasks>> GetTasksByStartDateAsync(DateTime startDate, int userId)
        {
            DateTime startOfDay = startDate.Date;
            DateTime endOfDay = startDate.Date.AddDays(1).AddTicks(-1); // End of the same day
            return await _context.Tasks
                .Where(t => t.StartDate >= startOfDay && t.StartDate <= endOfDay && t.UserId == userId)
                .OrderBy(t => t.StartDate)
                .ToListAsync();
        }
        #endregion

        #region Get tasks of user by end date
        public async Task<IEnumerable<Tasks>> GetTasksByEndDateAsync(DateTime endDate, int userId)
        {
            DateTime startOfDay = endDate.Date;
            DateTime endOfDay = endDate.Date.AddDays(1).AddTicks(-1); // End of the same day
            return await _context.Tasks
                .Where(t => t.EndDate >= startOfDay && t.EndDate <= endOfDay && t.UserId == userId)
                .OrderBy(t => t.EndDate)
                .ToListAsync();
        }
        #endregion

        #region Get tasks by date range
        public async Task<IEnumerable<Tasks>> GetTasksByDateRangeAsync(DateTime startDate, DateTime endDate, int userId)
        {
            return await _context.Tasks
                .Where(t => t.StartDate >= startDate && t.EndDate <= endDate && t.UserId == userId)
                .OrderBy(t => t.StartDate)
                .ToListAsync();
        }
        #endregion



        #region Soft delete...Also add backup table logic

        public async Task<bool> SoftDeleteTaskAsync(int taskId, int userId)

        {

            try

            {

                var taskToDelete = await _context.Tasks

                                                 .FirstOrDefaultAsync(t => t.TaskId == taskId && t.UserId == userId);

                var user = await _context.Users

                                         .FirstOrDefaultAsync(t => t.UserId == userId);

                if (taskToDelete != null && user != null)

                {

                    await _context.Database.ExecuteSqlRawAsync("CALL insert_into_backup({0});", taskId);

                    taskToDelete.EnabledFlag = "N";

                    taskToDelete.LastUpdationDate = DateTime.UtcNow;

                    taskToDelete.LastUpdatedBy = user.Username;

                    await _context.SaveChangesAsync();

                    return true;

                }

                return false;

            }

            catch (Exception)

            {

                return false;

            }

        }

        #endregion

        #region Multi select delete : Stored Procedure

        async Task<bool> ITaskRepository.MultiselectSoftDeleteTasksAsync(List<int> taskIds, int userId)
        {
            try
            {
                var tasksToUpdate = await _context.Tasks
                                                  .Where(t => taskIds.Contains(t.TaskId))
                                                  .ToListAsync();

                if (tasksToUpdate.Count == 0)
                {
                    return false;
                }

                var user = await _context.Users
                                         .FirstOrDefaultAsync(t => t.UserId == userId);

                if (user == null)
                {
                    return false;
                }

                foreach (var task in tasksToUpdate)
                {

                    await _context.Database.ExecuteSqlRawAsync("CALL insert_into_backup({0});", task.TaskId);

                    task.EnabledFlag = "N";
                    task.LastUpdationDate = DateTime.UtcNow;
                    task.LastUpdatedBy = user.Username;
                }

                await _context.SaveChangesAsync();
                return true;
            }
            catch (Exception)
            {
                return false;
            }
        }

        #endregion


        //NN

        #region Get task of user by title:Re
        public async Task<IEnumerable<Tasks>> GetTasksByTitleAsync(string title, int userId)
        {
            return await _context.Tasks
                         .Where(t => t.Title.Contains(title) && t.UserId == userId)
                         .OrderBy(t => t.EndDate)
                         .ToListAsync();
        }
        #endregion

       

        

        #region Delete Task
        public async Task<bool> DeleteTaskAsync(int taskId)
        {

            try
            {
                var Tasks = await _context.Tasks.FindAsync(taskId);
                if (Tasks != null)
                {
                    _context.Tasks.Remove(Tasks);
                    await _context.SaveChangesAsync();
                    return true;
                }
                return false;
            }
            catch (Exception)
            {
                return false;
            }
        }
        #endregion

        //#region Multi select delete :Ef core logic

        //async Task<bool> ITaskRepository.MultiselectSoftDeleteTasksAsync(List<int> taskIds, int userId)
        //{
        //    try
        //    {

        //        var TasksToUpdate = await _context.Tasks
        //                                          .Where(t => taskIds.Contains(t.TaskId))
        //                                          .ToListAsync();

        //        var user = await _context.Users.
        //               FirstOrDefaultAsync(t => t.UserId == userId);

        //        if (TasksToUpdate.Count == 0)
        //        {
        //            return false;
        //        }

        //        foreach (var Tasks in TasksToUpdate)
        //        {
        //            Tasks.EnabledFlag = "N";
        //            Tasks.LastUpdationDate = DateTime.UtcNow;
        //            Tasks.LastUpdatedBy = user.Username;
        //        }

        //        await _context.SaveChangesAsync();
        //        return true;
        //    }
        //    catch (Exception)
        //    {
        //        return true;
        //    }
        //}

        //#endregion

    }
}
