namespace TaskManager.CustomExceptions
{
    public class UserNotFoundException : Exception
    {
        public string UserIdentifier { get; }

        public UserNotFoundException(string userIdentifier)
            : base($"User not found for the identifier: {userIdentifier}")
        {
            UserIdentifier = userIdentifier;
        }

        public UserNotFoundException(string userIdentifier, Exception innerException)
            : base($"User not found for the identifier: {userIdentifier}", innerException)
        {
            UserIdentifier = userIdentifier;
        }
    }
}
