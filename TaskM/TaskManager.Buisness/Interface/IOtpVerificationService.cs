namespace TaskManager.Buisness.Interface
{
    public interface IOtpVerificationService
    {
        Task<bool> SaveOtpAsync(string email, string otp);
        Task<bool> ValidateOtpAsync(string email, string otp);
        Task MarkOtpAsUsedAsync(int otpId);
    }
}
