namespace TaskManager.Entities.ProfileDTO
{
    /// <summary>
    /// Represents the request data required to edit a user's profile.
    /// This DTO is used to transfer the updated user profile information from the client to the server.
    /// </summary>
    public class EditUserProfileRequestDTO
    {
        /// <summary>
        /// Gets or sets the user's first name.
        /// This field represents the user's given name and can be updated as part of the profile edit process.
        /// </summary>
        /// <example>John</example>
        public string? FirstName { get; set; }

        /// <summary>
        /// Gets or sets the user's last name.
        /// This field represents the user's family name or surname and can be updated as part of the profile edit process.
        /// </summary>
        /// <example>Doe</example>
        public string? LastName { get; set; }

        /// <summary>
        /// Gets or sets the user's username.
        /// This field represents the user's unique username within the system and can be updated as part of the profile edit process.
        /// </summary>
        /// <example>johndoe123</example>
        public string? Username { get; set; }

        /// <summary>
        /// Gets or sets the user's email address.
        /// This field represents the user's email address and can be updated as part of the profile edit process.
        /// </summary>
        /// <example>johndoe@example.com</example>
        public string? Email { get; set; }
    }
}
