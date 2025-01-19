#region namespace
using Microsoft.EntityFrameworkCore;
using TaskManager.DataAccess.Interface;
using TaskManager.Models.Data;
using TaskManager.Models.Models; 
#endregion

namespace TaskManager.DataAccess.Class
{
    public class OtpVerificationRepository : IOtpVrificationRepository
    {
        private readonly MyDbContext _context;
        public OtpVerificationRepository(MyDbContext context)
        {
            _context = context;
        }

        #region Save otp to database
        public async Task SaveOtpAsync(OtpTempTable otp)
        {
            _context.OtpTempTables.Add(otp);
            await _context.SaveChangesAsync();
        } 
        #endregion

        #region Get the latest OTP for a specific email and check if it's expired
        public async Task<OtpTempTable> GetLatestOtpByEmailAsync(string email)
        {
            var otp = await _context.OtpTempTables
                .Where(o => o.Email == email && o.IsUsed != "Y")
                .OrderByDescending(o => o.CreationDate)
                .FirstOrDefaultAsync();

            if (otp != null && otp.CreationDate.HasValue &&
                (DateTime.Now - otp.CreationDate.Value).TotalMinutes > 15)
            {

                await _context.SaveChangesAsync();
                return null;
            }

            return otp;
        } 
        #endregion

        #region Mark OTP Used
        public async Task MarkOtpAsUsedAsync(int otpId)
        {
            try
            {
                var otp = await _context.OtpTempTables.FindAsync(otpId);

                if (otp != null)
                {
                    otp.IsUsed = "Y"; 
                    _context.Entry(otp).State = EntityState.Modified;

                    Console.WriteLine($"Marking OTP {otpId} as used. IsUsed: {otp.IsUsed}");

                    await _context.SaveChangesAsync();
                    Console.WriteLine("Changes committed to database.");
                }
                else
                {
                    Console.WriteLine($"OTP with ID {otpId} not found.");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error marking OTP as used: {ex.Message}");
            }
        } 
        #endregion


    }
}
