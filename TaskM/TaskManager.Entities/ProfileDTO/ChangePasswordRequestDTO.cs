namespace TaskManager.Entities.ProfileDTO
{
    /// <summary>
    /// Represents a request to change the user's password.
    /// This class is used to transfer the required information for changing a user's password from the client to the server.
    /// </summary>
    public class ChangePasswordRequestDTO
    {
        /// <summary>
        /// The current password of the user that is being replaced.
        /// This field should contain the user's existing password, which will be validated before allowing the change.
        /// </summary>
        /// <example>OldPassword123!</example>
        public string OldPassword { get; set; }

        /// <summary>
        /// The new password to be set for the user.
        /// This field must meet the security requirements (e.g., minimum length, complexity) and will replace the old password upon validation.
        /// </summary>
        /// <example>NewPassword!2025</example>
        public string NewPassword { get; set; }

        /// <summary>
        /// A confirmation of the new password.
        /// This field must exactly match the <see cref="NewPassword"/> field to confirm that the user intends to set the new password.
        /// </summary>
        /// <example>NewPassword!2025</example>
        public string ConfirmPassword { get; set; }
    }
}
