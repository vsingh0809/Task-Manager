

namespace TaskManager.Entities.TaskDTO
{
    public class TaskResponseDTO
    {
        public int TaskId { get; set; }
        public int? UserId { get; set; }
        public string Title { get; set; } = null!;
        public string? Description { get; set; }
        public string Status { get; set; } = null!;
        public DateTime? StartDate { get; set; }
        public DateTime EndDate { get; set; }
    }
}