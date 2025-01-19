namespace TaskManager.Entities.TaskDTO
{
    public class SaveTaskRequestDTO
    {
      //  public int? UserId { get; set; }

        public string Title { get; set; } = null!;

        public string? Description { get; set; }

        public DateTime EndDate { get; set; }
    }
}
