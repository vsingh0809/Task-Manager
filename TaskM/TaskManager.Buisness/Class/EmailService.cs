
using MailKit.Security;
using Microsoft.Extensions.Options;
using MimeKit;
using MailKit.Net.Smtp;
using System.Net.Mail;
using SmtpClient = MailKit.Net.Smtp.SmtpClient;
using System.Text.RegularExpressions;
using TaskManager.Buisness.Interface;
using TaskManager.Entities;
using TaskManager.Common.CustomExceptions;

namespace TaskManager.Buisness.Class
{
    public class EmailService : IEmailService
    {
        private readonly EmailSettings emailSettings;
        private readonly IOtpVerificationService _otptempTableService;

        public EmailService(IOptions<EmailSettings> options, IOtpVerificationService otptempTableService)
        {
            emailSettings = options.Value;
            _otptempTableService = otptempTableService;
        }

        #region Send email
        public async Task SendEmail(MailRequest mailRequest)
        {
            // Validate email format before proceeding
            if (!IsValidEmail(mailRequest.Email))
            {
                // Log the error and throw a validation exception immediately
                Console.WriteLine($"Invalid email format: {mailRequest.Email}");
                throw new InvalidEmailFormatException($"Invalid email format: {mailRequest.Email}");
            }

            var email = new MimeMessage();
            email.Sender = MailboxAddress.Parse(emailSettings.Email);
            email.To.Add(MailboxAddress.Parse(mailRequest.Email));
            email.Subject = mailRequest.Subject;

            var builder = new BodyBuilder
            {
                TextBody = "This is the plain text version of the email.",
                HtmlBody = mailRequest.EmailBody
            };
            email.Body = builder.ToMessageBody();

            using var smtp = new SmtpClient();

            try
            {
                // Connect to SMTP server only if email format is valid
                Console.WriteLine("Attempting to connect to SMTP server...");
                smtp.Connect(emailSettings.Host, emailSettings.Port, SecureSocketOptions.StartTls);
            }
            catch (Exception ex)
            {
                // Connection failed, log it
                Console.WriteLine($"Error connecting to SMTP server: {ex.Message}");
                throw new SmtpConnectionException("Failed to connect to SMTP server.", ex);
            }

            try
            {
                // Authenticate with the SMTP server
                Console.WriteLine("Attempting to authenticate...");
                smtp.Authenticate(emailSettings.Email, emailSettings.Password);
            }
            catch (Exception ex)
            {
                // Authentication failed, log it
                Console.WriteLine($"SMTP authentication failed: {ex.Message}");
                throw new SmtpAuthenticationException("SMTP authentication failed.", ex);
            }

            try
            {
                // Send the email only if authentication succeeded
                Console.WriteLine("Sending email...");
                await smtp.SendAsync(email);
            }
            catch (SmtpCommandException ex)
            {
                // Handle specific SMTP command errors
                Console.WriteLine($"SMTP Command Error: {ex.Message}");
                Console.WriteLine($"StatusCode: {ex.StatusCode}");

                // Check if the error is related to recipient issues
                if (ex.ErrorCode == SmtpErrorCode.RecipientNotAccepted)
                {
                    Console.WriteLine("Recipient address not accepted.");
                    throw new EmailServiceException($"Recipient address {mailRequest.Email} not accepted.", ex);
                }
                else if (ex.ErrorCode == SmtpErrorCode.SenderNotAccepted)
                {
                    Console.WriteLine("Sender address not accepted.");
                    throw new EmailServiceException($"Sender address {email.Sender} not accepted.", ex);
                }
                else if (ex.ErrorCode == SmtpErrorCode.MessageNotAccepted)
                {
                    Console.WriteLine("Message content not accepted by the server.");
                    throw new EmailServiceException("Message content not accepted by the SMTP server.", ex);
                }
                else
                {
                    // Log other SMTP command errors
                    throw new EmailServiceException("SMTP command error occurred while sending the email.", ex);
                }
            }
            catch (SmtpProtocolException ex)
            {
                // Handle protocol-related errors (e.g., unexpected server responses)
                Console.WriteLine($"SMTP Protocol Error: {ex.Message}");
                throw new EmailServiceException("SMTP protocol error occurred while sending the email.", ex);
            }
            catch (Exception ex)
            {
                // General exception handling for unexpected errors
                Console.WriteLine($"General error sending email: {ex.Message}");
                throw new EmailServiceException("Error occurred while sending the email.", ex);
            }
            finally
            {
                // Disconnect from SMTP server after attempting to send email
                Console.WriteLine("Disconnecting from SMTP server...");
                smtp.Disconnect(true);
            }
        }

        #endregion

        #region Send email with otp
        public async Task SendOtpEmail(string userEmail, string Name)
        {
            // Validate email format before attempting to generate OTP
            if (!IsValidEmail(userEmail))
            {
                // If email is invalid, log the error and throw an exception
                Console.WriteLine($"Invalid email format: {userEmail}");
                throw new InvalidEmailFormatException($"Invalid email format: {userEmail}");
            }

            // Generate OTP
            string OtpText = GenerateRandomNumber();
            if (string.IsNullOrEmpty(OtpText))
            {
                Console.WriteLine("Failed to generate OTP.");
                throw new OtpGenerationException("Failed to generate OTP.");
            }


            // Save OTP to the database using the service

            bool isOtpSaved = await _otptempTableService.SaveOtpAsync(userEmail, OtpText);

            if (!isOtpSaved)
            {
                throw new Exception("Failed to save OTP.");
            }

            // Prepare the email request with OTP and other details
            var mailRequest = new MailRequest
            {
                Email = userEmail,
                Subject = "Email Verification: OTP",
                EmailBody = GenerateEmailBody(Name, OtpText)
            };

            // Call SendEmail to actually send the OTP email
            Console.WriteLine("Sending OTP email...");
            await SendEmail(mailRequest);
        } 
        #endregion

        #region vaild email check
        public bool IsValidEmail(string email)
        {
            try
            {
                /*// Attempt to create a MailAddress object
                var mailAddress = new System.Net.Mail.MailAddress(email);
                // If no exception occurs, the email format is valid
                  return true;*/

                const string regex = @"^[^@\s]+@[^@\s]+\.(com|net|org|gov)$";
                return Regex.IsMatch(email, regex, RegexOptions.IgnoreCase);

            }
            catch (FormatException)
            {
                return false;
            }
        } 
        #endregion

        #region Generate Random Number for otp
        private string GenerateRandomNumber()
        {
            Random random = new Random();
            string randomNo = random.Next(0, 1000000).ToString("D6");
            return randomNo;
        } 
        #endregion

        #region Generate email body
        public string GenerateEmailBody(string Name, string OtpText)
        {
            string emailBody = string.Empty;

            emailBody += "this is sent";
            emailBody += "<div style='width:100%;background-color:#f2f2f2;padding:20px;box-sizing:border-box;font-family:Arial, sans-serif'>";
            emailBody += "<div style='max-width:600px;margin:auto;padding:20px;background-color:white;border-radius:8px;box-shadow:0 4px 8px rgba(0, 0, 0, 0.1)'>";
            emailBody += "<h1 style='color:#333;text-align:center;'>Hi " + Name + ",</h1>";
            emailBody += "<p style='font-size:16px;color:#333;'>Welcome to the <strong>Task Manager</strong>!</p>";
            emailBody += "<p style='font-size:16px;color:#333;'>Please enter the OTP below :</p>";
            emailBody += "<h2 style='color:#2796C9;text-align:center;font-size:30px;'>Your OTP : " + OtpText + "</h2>";
            emailBody += "<p style='font-size:16px;color:#333;text-align:center;'>OTP will be valid for 15 minutes!</p>";
            emailBody += "<hr style='border:1px solid #ddd;'>";
            emailBody += "<h4 style='font-size:14px;color:#555;text-align:center;'>This is an autogenerated email. Do not reply to this email.</h4>";
            emailBody += "</div>";
            emailBody += "</div>";

            return emailBody;
        } 
        #endregion

    }
}
