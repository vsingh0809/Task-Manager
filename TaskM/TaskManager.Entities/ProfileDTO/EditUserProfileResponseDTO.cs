namespace TaskManager.Entities.ProfileDTO
{
    /// <summary>
    /// Represents the response returned after attempting to edit a user's profile.
    /// This DTO encapsulates the result of the profile edit operation, including whether the operation was successful and a message describing the result.
    /// </summary>
    public class EditUserProfileResponseDTO
    {
        /// <summary>
        /// Gets or sets a value indicating whether the profile edit operation was successful.
        /// </summary>
        /// <value><c>true</c> if the profile edit was successful; otherwise, <c>false</c>.</value>
        /// <example>true</example>
        public bool IsSuccess { get; set; }

        /// <summary>
        /// Gets or sets a message providing additional information about the result of the profile edit operation.
        /// This message can indicate success or describe an error (e.g., invalid input, database failure).
        /// </summary>
        /// <example>"Profile updated successfully."</example>
        public string Message { get; set; }
    }
}
