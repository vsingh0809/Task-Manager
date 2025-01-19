namespace TaskManager.Entities.ProfileDTO
{
    /// <summary>
    /// Represents a user's profile details. This DTO contains personal and metadata information about a user, including their name, username, email, profile photo, creation and update timestamps, and status information.
    /// </summary>
    public class UserProfileDTO
    {
        /// <summary>
        /// Gets or sets the unique identifier for the user.
        /// </summary>
        /// <value>The unique identifier of the user.</value>
        /// <example>1</example>
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the first name of the user.
        /// </summary>
        /// <value>The user's first name.</value>
        /// <example>"John"</example>
        public string FirstName { get; set; }

        /// <summary>
        /// Gets or sets the last name of the user.
        /// </summary>
        /// <value>The user's last name.</value>
        /// <example>"Doe"</example>
        public string LastName { get; set; }

        /// <summary>
        /// Gets or sets the email address of the user.
        /// </summary>
        /// <value>The user's email address.</value>
        /// <example>"john.doe@example.com"</example>
        public string Email { get; set; }

        /// <summary>
        /// Gets or sets the username chosen by the user.
        /// </summary>
        /// <value>The user's username.</value>
        /// <example>"johndoe"</example>
        public string Username { get; set; }

        /// <summary>
        /// Gets or sets the profile photo of the user as a byte array.
        /// This is an optional property that contains the image data for the user's profile photo, if available.
        /// </summary>
        /// <value>A byte array representing the user's profile photo.</value>
        /// <example>[byte array data]</example>
        public string? Image { get; set; }

        /// <summary>
        /// Gets or sets the username of the person who created this user's profile.
        /// </summary>
        /// <value>The username of the creator of the user's profile.</value>
        /// <example>"admin"</example>
        public string CreatedBy { get; set; }

        /// <summary>
        /// Gets or sets the date and time when the user's profile was created.
        /// </summary>
        /// <value>The date and time when the profile was created.</value>
        /// <example>"2023-01-01T12:00:00Z"</example>
        public DateTime CreationDate { get; set; }

        /// <summary>
        /// Gets or sets the username of the person who last updated the user's profile.
        /// </summary>
        /// <value>The username of the last person who updated the profile.</value>
        /// <example>"admin"</example>
        public string LastUpdatedBy { get; set; }

        /// <summary>
        /// Gets or sets the date and time when the user's profile was last updated.
        /// </summary>
        /// <value>The date and time when the profile was last updated.</value>
        /// <example>"2023-02-01T14:30:00Z"</example>
        public DateTime LastUpdatedDate { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether the user profile operation was successful.
        /// </summary>
        /// <value><c>true</c> if the operation was successful; otherwise, <c>false</c>.</value>
        /// <example>true</example>
        public bool IsSuccess { get; set; }

        /// <summary>
        /// Gets or sets a message providing additional information about the profile operation, such as success or failure reason.
        /// </summary>
        /// <value>A message indicating the result of the operation.</value>
        /// <example>"Profile retrieved successfully." or "Error: Profile not found."</example>
        public string Message { get; set; }
    }
}
