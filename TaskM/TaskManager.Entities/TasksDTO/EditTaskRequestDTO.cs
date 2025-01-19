
namespace TaskManager.Entities.TaskDTO
{
    public class EditTaskRequestDTO
    {
       // public uint? UserId { get; set; }
        public int TaskId { get; set; }
        public string Title { get; set; } = null!;
        public string? Description { get; set; }
        public string Status { get; set; } = null!;
        public DateTime EndDate { get; set; }
    }
}