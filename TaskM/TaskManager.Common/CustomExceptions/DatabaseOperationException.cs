namespace TaskManager.CustomExceptions
{
    public class DatabaseOperationException : Exception
    {
        public DatabaseOperationException(string message, Exception innerException = null) : base(message, innerException)
        {

        }
    }
}

