using System;
using System.Collections.Generic;

namespace TaskManager.Models.Models;

public partial class TasksBackup
{
    public int TaskId { get; set; }

    public string? TaskTitle { get; set; }

    public string? TaskDescription { get; set; }

    public string? TaskStatus { get; set; }

    public DateTime? StartDate { get; set; }

    public DateTime? EndDate { get; set; }

    public string? CreatedBy { get; set; }

    public DateTime? BackedupOn { get; set; }
}
