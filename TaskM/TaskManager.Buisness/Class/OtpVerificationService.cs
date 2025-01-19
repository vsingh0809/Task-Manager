
#region namespace
using TaskManager.Buisness.Interface;
using TaskManager.DataAccess.Interface;
using TaskManager.Models.Models; 
#endregion

namespace TaskManager.Buisness.Class
{
    public class OtpVerificationService:IOtpVerificationService
    {
        private readonly IOtpVrificationRepository _otpTempTableRepository;

        public OtpVerificationService(IOtpVrificationRepository otpTempTableRepository)
        {
            _otpTempTableRepository = otpTempTableRepository;
        }

        #region Save OTP to the database
        public async Task<bool> SaveOtpAsync(string email, string otp)
        {
            var otpTemp = new OtpTempTable
            {
                Email = email,
                GeneratedOtp = long.Parse(otp),
                CreationDate = DateTime.Now,
                IsUsed = "N",
            };

            try
            {
                await _otpTempTableRepository.SaveOtpAsync(otpTemp);
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error saving OTP: {ex.Message}");
                return false;
            }
        }
        #endregion

        #region  Validate OTP for an email address
        public async Task<bool> ValidateOtpAsync(string email, string otp)
        {
            var storedOtp = await _otpTempTableRepository.GetLatestOtpByEmailAsync(email);

            if (storedOtp == null || storedOtp.GeneratedOtp != long.Parse(otp))
            {
                return false;
            }

            if (storedOtp.IsUsed == "Y" || (DateTime.Now - storedOtp.CreationDate.Value).TotalMinutes > 15)
            {
                int id = storedOtp.OtpId;
                await _otpTempTableRepository.MarkOtpAsUsedAsync(id);
                return false;
            }

            return true;
        }
        #endregion

        #region Mark OTP as used 
        public async System.Threading.Tasks.Task MarkOtpAsUsedAsync(int otpId)
        {
            await _otpTempTableRepository.MarkOtpAsUsedAsync(otpId);
        } 
        #endregion

    }
}
