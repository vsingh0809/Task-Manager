
#region namespace
using TaskManager.Models.Models;
#endregion

namespace TaskManager.DataAccess.Interface
{
    public interface IOtpVrificationRepository
    {
        Task SaveOtpAsync(OtpTempTable otp);
        Task<OtpTempTable> GetLatestOtpByEmailAsync(string email);
        Task MarkOtpAsUsedAsync(int otpId);

    }
}
