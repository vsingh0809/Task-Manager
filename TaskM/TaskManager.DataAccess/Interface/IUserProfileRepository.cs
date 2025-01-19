using TaskManager.Models.Models;

namespace TaskManager.DataAccess.Interface
{
    /// <summary>
    /// The <see cref="IUserProfileRepository"/> interface defines the contract for the data access layer 
    /// related to user profile operations. It provides methods for retrieving, updating, and modifying user profile 
    /// information within the persistence layer (e.g., database).
    /// </summary>
    public interface IUserProfileRepository
    {
        #region Profile Retrieval

        /// <summary>
        /// Retrieves the profile information of a user based on their unique user ID.
        /// This method fetches the full profile data of a user from the data store.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose profile is being requested.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is the <see cref="Users"/> object
        /// containing the user's profile data, or null if no user is found with the specified ID.
        /// </returns>
        /// <exception cref="ArgumentException">Thrown if the user ID is invalid or the user does not exist.</exception>
        Task<Users> GetUserProfileByIdAsync(int userId);

        #endregion

        #region Profile Update

        /// <summary>
        /// Updates the user profile data in the database. This method updates fields like name, email, etc., 
        /// for the user with the given <paramref name="user"/> object.
        /// </summary>
        /// <param name="user">The <see cref="Users"/> object containing the updated profile information.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a boolean indicating whether the
        /// update was successful. Returns <c>true</c> if the update was successful; otherwise, returns <c>false</c>.
        /// </returns>
        /// <exception cref="ArgumentException">Thrown if the user object is invalid or if there is an error updating the database.</exception>
        Task<bool> UpdateUserAsync(Users user);

        #endregion

        #region Password Change

        /// <summary>
        /// Changes the password of a user identified by their <paramref name="userId"/>.
        /// This method updates the user's password in the database with a new password.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose password is being changed.</param>
        /// <param name="newPassword">The new password that will replace the old password for the user.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a boolean indicating whether the
        /// password change was successful. Returns <c>true</c> if the password change was successful; otherwise, returns <c>false</c>.
        /// </returns>
        /// <exception cref="ArgumentException">Thrown if the user ID is invalid or if there is an error changing the password.</exception>
        Task<bool> ChangeUserPasswordAsync(int userId, string newPassword);

        #endregion

        #region Profile Retrieval by Email

        /// <summary>
        /// Retrieves the user profile by their email address.
        /// </summary>
        /// <param name="email">The email address of the user.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is the <see cref="Users"/> object
        /// containing the user's profile data, or <c>null</c> if no user is found with the specified email.
        /// </returns>
        Task<Users> GetByEmailAsync(string email);

        #endregion
    }
}

