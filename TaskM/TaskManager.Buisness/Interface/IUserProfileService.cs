using Microsoft.AspNetCore.Http;
using TaskManager.Entities.ProfileDTO;

namespace TaskManager.Buisness.Interface
{
    /// <summary>
    /// Defines the contract for managing user profile-related operations in the system. 
    /// This interface is implemented by services that handle business logic for user profile actions.
    /// It provides methods for retrieving, updating, changing passwords, and managing profile photos.
    /// </summary>
    public interface IUserProfileService
    {
        #region Profile Retrieval

        /// <summary>
        /// Retrieves the profile details of a user based on the given user ID.
        /// This method handles the business logic to fetch user profile information.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose profile needs to be retrieved.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <see cref="UserProfileDTO"/> 
        /// containing the user's profile details, such as first name, last name, email, username, etc.
        /// </returns>
        /// <exception cref="UserNotFoundException">Thrown if the user with the given ID does not exist.</exception>
        Task<UserProfileDTO> GetUserProfileAsync(int userId);

        #endregion

        #region Profile Update

        /// <summary>
        /// Updates the user profile with the provided details.
        /// This method handles the logic of updating the user’s profile in the business layer, 
        /// such as validation of new data and invoking the appropriate data access methods.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose profile needs to be updated.</param>
        /// <param name="request">The request object containing updated user information like first name, last name, email, and username.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is an <see cref="EditUserProfileResponseDTO"/> 
        /// containing the result of the profile update operation, including success status and any validation messages.
        /// </returns>
        /// <exception cref="ValidationException">Thrown if the provided profile data is invalid (e.g., email format issues, required fields missing).</exception>
        /// <exception cref="UserNotFoundException">Thrown if the user with the given ID does not exist.</exception>
        Task<EditUserProfileResponseDTO> EditUserProfileAsync(int userId, EditUserProfileRequestDTO request);

        #endregion

        #region Profile Photo Management

        /// <summary>
        /// Adds or updates the profile photo for a user. 
        /// This method handles the logic of uploading the new photo (or updating an existing one) 
        /// and ensures that the photo complies with supported file formats (JPEG/PNG).
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose profile photo is being added or updated.</param>
        /// <param name="photo">The photo file being uploaded (should be of type .jpeg or .png).</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <see cref="ProfilePhotoResponseDTO"/> 
        /// containing the result of the photo upload operation, including success status and the URL or ID of the uploaded photo.
        /// </returns>
        /// <exception cref="UnsupportedFileFormatException">Thrown if the provided photo file is not of type JPEG or PNG.</exception>
        /// <exception cref="UserNotFoundException">Thrown if the user with the given ID does not exist.</exception>
        Task<ProfilePhotoResponseDTO> AddOrUpdateProfilePhotoAsync(int userId, IFormFile photo);

        /// <summary>
        /// Removes the profile photo of a user. 
        /// This method handles the logic of deleting the current profile photo and updating the user's profile accordingly.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose profile photo needs to be removed.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <see cref="ProfilePhotoResponseDTO"/> 
        /// indicating whether the photo removal was successful or not.
        /// </returns>
        /// <exception cref="UserNotFoundException">Thrown if the user with the given ID does not exist.</exception>
        Task<ProfilePhotoResponseDTO> RemoveProfilePhotoAsync(int userId);

        #endregion

        #region Password Change

        /// <summary>
        /// Changes the password for a user.
        /// This method validates the user's request, ensuring that the old password matches the existing one,
        /// and the new password meets the system's security criteria.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose password is to be changed.</param>
        /// <param name="request">The request object containing the new password and the confirmation password.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <see cref="ChangePasswordResponseDTO"/> 
        /// containing the result of the password change operation, including a success flag and any error messages.
        /// </returns>
        /// <exception cref="ValidationException">Thrown if the new password does not meet system requirements or if the old password is incorrect.</exception>
        /// <exception cref="UserNotFoundException">Thrown if the user with the given ID does not exist.</exception>
        Task<ChangePasswordResponseDTO> ChangePasswordAsync(int userId, ChangePasswordRequestDTO request);

        #endregion
    }
}
