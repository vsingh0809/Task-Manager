
using TaskManager.Entities;

namespace TaskManager.Buisness.Interface
{
    public interface IEmailService
    {
        Task SendEmail(MailRequest mailRequest);
        Task SendOtpEmail(string userEmail, string Name);
    }
}
