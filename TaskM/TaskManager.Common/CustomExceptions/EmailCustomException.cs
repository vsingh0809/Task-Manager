namespace TaskManager.Common.CustomExceptions
{
    // Base custom exception for email-related errors
    public class EmailServiceException : Exception
    {
        public EmailServiceException(string message) : base(message) { }
        public EmailServiceException(string message, Exception innerException) : base(message, innerException) { }
    }

    // Specific custom exception for invalid email format errors
    public class InvalidEmailFormatException : EmailServiceException
    {
        public InvalidEmailFormatException(string message) : base(message) { }
    }

    // Specific custom exception for SMTP connection issues
    public class SmtpConnectionException : EmailServiceException
    {
        public SmtpConnectionException(string message) : base(message)
        {
        }

        public SmtpConnectionException(string message, Exception ex) : base(message) { }
    }

    // Specific custom exception for authentication errors
    public class SmtpAuthenticationException : EmailServiceException
    {
        public SmtpAuthenticationException(string message) : base(message)
        {
        }

        public SmtpAuthenticationException(string message, Exception ex) : base(message) { }
    }

    // Specific custom exception for failures while sending OTP
    public class OtpGenerationException : EmailServiceException
    {
        public OtpGenerationException(string message) : base(message) { }
    }
}
