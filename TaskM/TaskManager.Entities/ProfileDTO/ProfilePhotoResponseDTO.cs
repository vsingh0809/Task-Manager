namespace TaskManager.Entities.ProfileDTO
{
    /// <summary>
    /// Represents the response for operations related to profile photos, such as adding, updating, or removing a user's profile photo.
    /// This DTO encapsulates the result of the photo operation, including whether the operation was successful, a status message, and optionally, the image data.
    /// </summary>
    public class ProfilePhotoResponseDTO
    {
        /// <summary>
        /// Gets or sets a value indicating whether the profile photo operation was successful.
        /// </summary>
        /// <value><c>true</c> if the operation was successful; otherwise, <c>false</c>.</value>
        /// <example>true</example>
        public bool IsSuccess { get; set; }

        /// <summary>
        /// Gets or sets a message providing additional information about the result of the operation.
        /// This can indicate success or describe an error (e.g., unsupported file type, server failure).
        /// </summary>
        /// <example>"Profile photo updated successfully."</example>
        public string Message { get; set; }

        /// <summary>
        /// Gets or sets the profile photo image as a byte array.
        /// This property is optional and may be returned if the operation involves retrieving or confirming the profile photo.
        /// </summary>
        /// <example>
        /// A byte array representing the profile photo image (e.g., image data in JPEG or PNG format).
        /// </example>
        public string? Image { get; set; }
    }
}
