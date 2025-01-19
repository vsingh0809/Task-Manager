using System;
using System.Collections.Generic;

namespace TaskManager.Models.Models;

public partial class Tasks
{
    public int TaskId { get; set; }

    public int? UserId { get; set; }

    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public string Status { get; set; } = null!;

    public DateTime? StartDate { get; set; }

    public DateTime EndDate { get; set; }

    public string? EnabledFlag { get; set; }

    public string? CreatedBy { get; set; }

    public DateTime? CreationDate { get; set; }

    public string? LastUpdatedBy { get; set; }

    public DateTime? LastUpdationDate { get; set; }

    public virtual Users? User { get; set; }
}
