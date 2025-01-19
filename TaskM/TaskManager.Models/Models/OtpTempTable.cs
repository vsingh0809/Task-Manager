using System;
using System.Collections.Generic;

namespace TaskManager.Models.Models;

public partial class OtpTempTable
{
    public int OtpId { get; set; }

    public string Email { get; set; } = null!;

    public long GeneratedOtp { get; set; }

    public string? IsUsed { get; set; }

    public DateTime? CreationDate { get; set; }
}
