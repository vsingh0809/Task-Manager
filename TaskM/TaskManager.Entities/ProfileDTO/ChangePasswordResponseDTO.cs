namespace TaskManager.Entities.ProfileDTO
{
    /// <summary>
    /// Represents the response returned after attempting to change the user's password.
    /// This class encapsulates the result of the password change operation, including whether the operation was successful and a message describing the result.
    /// </summary>
    public class ChangePasswordResponseDTO
    {
        /// <summary>
        /// Gets or sets a value indicating whether the password change operation was successful.
        /// </summary>
        /// <value><c>true</c> if the password change was successful; otherwise, <c>false</c>.</value>
        /// <example>true</example>
        public bool IsSuccess { get; set; }

        /// <summary>
        /// Gets or sets a message providing additional information about the result of the password change operation.
        /// This message can indicate success or describe an error, such as invalid old password, password mismatch, etc.
        /// </summary>
        /// <example>"Password changed successfully."</example>
        public string Message { get; set; }
    }
}
