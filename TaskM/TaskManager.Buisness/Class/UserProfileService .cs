using TaskManager.Buisness.Interface;
using Microsoft.AspNetCore.Http;
using TaskManager.DataAccess.Interface;
using TaskManager.Entities.ProfileDTO;
using Microsoft.AspNetCore.Identity;
using TaskManager.Models.Models;
using Microsoft.Extensions.Logging;

namespace TaskManager.Business.Class
{
    /// <summary>
    /// UserProfileService contains business logic for managing user profiles. It is responsible for interacting with the data access layer
    /// and returning responses related to user profiles, such as fetching, updating, changing passwords, and managing profile photos.
    /// This service is intended to be used by controllers like <see cref="UserProfileController"/>.
    /// </summary>
    public class UserProfileService : IUserProfileService
    {
        private readonly IUserProfileRepository _userProfileRepository;
        private readonly IPasswordHasher<Users> _passwordHasher;
        private readonly ILogger<UserProfileService> _logger;

        /// <summary>
        /// Initializes a new instance of the <see cref="UserProfileService"/> class.
        /// </summary>
        /// <param name="userProfileRepository">The user profile repository that interacts with the data access layer.</param>
        public UserProfileService(IUserProfileRepository userProfileRepository, IPasswordHasher<Users> passwordHasher, ILogger<UserProfileService> logger)
        {
            _userProfileRepository = userProfileRepository;
            _passwordHasher = passwordHasher;
            _logger = logger;
        }

        #region **User Profile Retrieval**

        /// <summary>
        /// Retrieves the profile details for a user based on the provided user ID.
        /// If the user is not found, a message indicating the failure is returned.
        /// </summary>
        /// <param name="userId">The ID of the user whose profile is to be fetched.</param>
        /// <returns>Returns a <see cref="UserProfileDTO"/> containing the user profile information.</returns>
        /// <exception cref="KeyNotFoundException">Thrown if the user with the given user ID is not found in the system.</exception>
        public async Task<UserProfileDTO> GetUserProfileAsync(int userId)
        {
            _logger.LogInformation("Fetching user profile for UserId {UserId}.", userId); // Log profile fetch request

            var user = await _userProfileRepository.GetUserProfileByIdAsync(userId);

            if (user == null)
            {
                _logger.LogWarning("User profile not found for UserId {UserId}.", userId); // Log user not found
                return new UserProfileDTO
                {
                    IsSuccess = false,
                    Message = "User not found"
                };
            }
            _logger.LogInformation("User profile fetched successfully for UserId {UserId}.", userId); // Log successful profile fetch

            return new UserProfileDTO
            {
                UserId = user.UserId,
                FirstName = user.FirstName,
                LastName = user.LastName,
                Email = user.Email,
                Username = user.Username,
                //Image = user.Image,
                Image = user.Image != null ? Convert.ToBase64String(user.Image) : null,
                CreatedBy = user.CreatedBy,
                CreationDate = user.CreationDate ?? DateTime.MinValue,
                LastUpdatedBy = user.LastUpdatedBy ?? string.Empty,
                LastUpdatedDate = user.LastUpdatedDate ?? DateTime.MinValue,
                IsSuccess = true,
                Message = "User profile fetched successfully"
            };
        }

        #endregion

        #region **User Profile Editing**

        /// <summary>
        /// Updates the user's profile information based on the provided request.
        /// Only the fields present in the request are updated.
        /// </summary>
        /// <param name="userId">The ID of the user whose profile is to be updated.</param>
        /// <param name="request">The updated profile information sent from the client (e.g., name, email, username).</param>
        /// <returns>Returns an <see cref="EditUserProfileResponseDTO"/> with a success message or error message.</returns>
        /// <exception cref="KeyNotFoundException">Thrown if the user with the given user ID is not found in the system.</exception>
        public async Task<EditUserProfileResponseDTO> EditUserProfileAsync(int userId, EditUserProfileRequestDTO request)
        {
            _logger.LogInformation("Editing user profile for UserId {UserId}.", userId); // Log profile edit request
            var user = await _userProfileRepository.GetUserProfileByIdAsync(userId);

            if (user == null)
            {
                _logger.LogWarning("User profile not found for UserId {UserId}.", userId); // Log user not found
                return new EditUserProfileResponseDTO
                {
                    IsSuccess = false,
                    Message = "User not found"
                };
            }
            if (user.Email != request.Email)
            {
                var userWithSameEmail = await _userProfileRepository.GetByEmailAsync(request.Email);
                if (userWithSameEmail != null)
                {
                    _logger.LogError("Attempt to update profile with an already used email: {Email}.", request.Email); // Log email conflict error
                    throw new InvalidOperationException("Email is already in use by another user.");
                }
            }

            // Update the fields only if they are provided
            if (!string.IsNullOrEmpty(request.FirstName)) user.FirstName = request.FirstName;
            if (!string.IsNullOrEmpty(request.LastName)) user.LastName = request.LastName;
            if (!string.IsNullOrEmpty(request.Username)) user.Username = request.Username;
            if (!string.IsNullOrEmpty(request.Email)) user.Email = request.Email;

            // Track who updated the profile and when
            user.LastUpdatedBy = userId.ToString(); // Assuming userId is the ID of the authenticated user.
            user.LastUpdatedDate = DateTime.Now;

            var success = await _userProfileRepository.UpdateUserAsync(user);
            if (success)
            {
                _logger.LogInformation("User profile updated successfully for UserId {UserId}.", userId); // Log successful profile update
            }
            else
            {
                _logger.LogError("Failed to update user profile for UserId {UserId}.", userId); // Log failed profile update
            }

            return new EditUserProfileResponseDTO
            {
                IsSuccess = success,
                Message = success ? "Profile updated successfully" : "Profile update failed"
            };
        }

        #endregion

        #region **Profile Photo Management**

        /// <summary>
        /// This method adds or updates a user's profile photo. It ensures that the photo is of the correct file type (.jpeg or .png)
        /// and does not exceed the size limit (10MB). The photo is stored as a byte array in the user’s profile.
        /// </summary>
        /// <param name="userId">The ID of the user whose profile photo is being uploaded.</param>
        /// <param name="photo">The photo file (image) that the user is uploading.</param>
        /// <returns>Returns a <see cref="ProfilePhotoResponseDTO"/> indicating whether the upload was successful or not.</returns>
        /// <exception cref="KeyNotFoundException">Thrown if the user with the given user ID is not found in the system.</exception>
        public async Task<ProfilePhotoResponseDTO> AddOrUpdateProfilePhotoAsync(int userId, IFormFile photo)
        {
            _logger.LogInformation("Adding or updating profile photo for UserId {UserId}.", userId); // Log photo upload request
            // Validate file extension
            if (photo == null || (photo.ContentType != "image/jpeg" && photo.ContentType != "image/png" && photo.ContentType != "image/jpg"))
            {
                return new ProfilePhotoResponseDTO
                {
                    IsSuccess = false,
                    Message = "Invalid file format. Only .jpg, .jpeg or .png are allowed."
                };
            }

            // Validate file size (60kB)
            if (photo.Length > 60 * 1024)
            {
                _logger.LogWarning("File size exceeds 60KB for UserId {UserId}. File size: {FileSize}KB.", userId, photo.Length / 1024);
                return new ProfilePhotoResponseDTO
                {
                    IsSuccess = false,
                    Message = "File size exceeds 60kB."
                };
            }

            var user = await _userProfileRepository.GetUserProfileByIdAsync(userId);
            if (user == null)
            {
                _logger.LogWarning("User not found for UserId {UserId}.", userId); // Log user not found
                return new ProfilePhotoResponseDTO
                {
                    IsSuccess = false,
                    Message = "User not found."
                };
            }

            // Store the image as a byte array in the user profile
            using (var memoryStream = new MemoryStream())
            {
                await photo.CopyToAsync(memoryStream);
                user.Image = memoryStream.ToArray();
            }

            var success = await _userProfileRepository.UpdateUserAsync(user);

            return new ProfilePhotoResponseDTO
            {
                IsSuccess = success,
                Message = success ? "Profile photo updated successfully" : "Failed to update profile photo."
            };
        }

        /// <summary>
        /// Removes the user's profile photo. This method clears the image stored for the user.
        /// </summary>
        /// <param name="userId">The ID of the user whose profile photo is to be removed.</param>
        /// <returns>Returns a <see cref="ProfilePhotoResponseDTO"/> indicating whether the removal was successful or not.</returns>
        /// <exception cref="KeyNotFoundException">Thrown if the user with the given user ID is not found in the system.</exception>
        public async Task<ProfilePhotoResponseDTO> RemoveProfilePhotoAsync(int userId)
        {
            var user = await _userProfileRepository.GetUserProfileByIdAsync(userId);
            if (user == null)
            {
                return new ProfilePhotoResponseDTO
                {
                    IsSuccess = false,
                    Message = "User not found."
                };
            }

            user.Image = null;
            var success = await _userProfileRepository.UpdateUserAsync(user);

            return new ProfilePhotoResponseDTO
            {
                IsSuccess = success,
                Message = success ? "Profile photo removed successfully" : "Failed to remove profile photo."
            };
        }

        #endregion

        #region **Password Management**

        /// <summary>
        /// Changes the password for the user. The method validates the new password to ensure it contains at least one letter and one number.
        /// It also checks that the new password matches the confirmation password.
        /// </summary>
        /// <param name="userId">The ID of the user whose password is to be changed.</param>
        /// <param name="request">The request containing the new password and confirmation password.</param>
        /// <returns>Returns a <see cref="ChangePasswordResponseDTO"/> indicating whether the password change was successful or not.</returns>
        public async Task<ChangePasswordResponseDTO> ChangePasswordAsync(int userId, ChangePasswordRequestDTO request)
        {
            _logger.LogInformation("Changing password for UserId {UserId}.", userId); // Log password change request
            // Check if the old password and new password are provided
            if (string.IsNullOrWhiteSpace(request.OldPassword) || string.IsNullOrWhiteSpace(request.NewPassword))
            {
                _logger.LogWarning("Old password or new password is empty for UserId {UserId}.", userId); // Log warning for empty password fields
                return new ChangePasswordResponseDTO
                {
                    IsSuccess = false,
                    Message = "Old password and new password cannot be empty."
                };
            }

            // Retrieve the user by userId
            var user = await _userProfileRepository.GetUserProfileByIdAsync(userId);
            if (user == null)
            {
                _logger.LogWarning("User not found for UserId {UserId}.", userId); // Log user not found
                return new ChangePasswordResponseDTO
                {
                    IsSuccess = false,
                    Message = "User not found."
                };
            }

            // Verify the old password using the IPasswordHasher
            var passwordVerificationResult = _passwordHasher.VerifyHashedPassword(user, user.Password, request.OldPassword);
            if (passwordVerificationResult != PasswordVerificationResult.Success)
            {
                _logger.LogWarning("Incorrect old password for UserId {UserId}.", userId); // Log incorrect old password
                return new ChangePasswordResponseDTO
                {
                    IsSuccess = false,
                    Message = "Incorrect old password."
                };
            }

            // Validate new password: must contain at least one letter and one number
            if (!request.NewPassword.Any(char.IsLetter) || !request.NewPassword.Any(char.IsDigit))
            {
                _logger.LogWarning("New password does not meet criteria for UserId {UserId}.", userId); // Log password validation failure
                return new ChangePasswordResponseDTO
                {
                    IsSuccess = false,
                    Message = "New password must contain at least one letter and one number."
                };
            }

            // Ensure that the new password and confirm password match
            if (request.NewPassword != request.ConfirmPassword)
            {
                _logger.LogWarning("Password confirmation does not match for UserId {UserId}.", userId); // Log password mismatch
                return new ChangePasswordResponseDTO
                {
                    IsSuccess = false,
                    Message = "Password confirmation does not match."
                };
            }

            // Hash the new password before saving
            var hashedPassword = _passwordHasher.HashPassword(user, request.NewPassword);

            // Update the user's password in the database
            var success = await _userProfileRepository.ChangeUserPasswordAsync(userId, hashedPassword);

            if (success)
            {
                _logger.LogInformation("Password changed successfully for UserId {UserId}.", userId); // Log successful password change
            }
            else
            {
                _logger.LogError("Failed to change password for UserId {UserId}.", userId); // Log failed password change
            }
            return new ChangePasswordResponseDTO
            {
                IsSuccess = success,
                Message = success ? "Password changed successfully" : "Failed to change password."
            };
        }

        #endregion
    }
}
