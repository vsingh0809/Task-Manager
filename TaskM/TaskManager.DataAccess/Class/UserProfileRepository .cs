using TaskManager.DataAccess.Interface;
using TaskManager.Models.Models;
using TaskManager.Models.Data;
using Microsoft.EntityFrameworkCore;
namespace TaskManager.DataAccess.Class
{
    /// <summary>
    /// The <see cref="UserProfileRepository"/> class implements the <see cref="IUserProfileRepository"/> interface.
    /// It provides methods for retrieving, updating, and changing the password of a user in the database.
    /// This class interacts with the database using the <see cref="MyDbContext"/> to manage user profile-related data.
    /// </summary>
    public class UserProfileRepository : IUserProfileRepository
    {
        private readonly MyDbContext _context;

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="UserProfileRepository"/> class with the provided database context.
        /// This constructor is used to inject the <see cref="MyDbContext"/> dependency into the repository.
        /// </summary>
        /// <param name="context">An instance of <see cref="MyDbContext"/> used to interact with the database.</param>
        public UserProfileRepository(MyDbContext context)
        {
            _context = context;
        }

        #endregion

        #region Profile Retrieval

        /// <summary>
        /// Retrieves the profile details of a user from the database based on their user ID.
        /// This method performs a lookup in the <see cref="Users"/> table using the provided user ID.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose profile needs to be retrieved.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <see cref="Users"/> object containing the user’s profile details,
        /// or <c>null</c> if no user was found with the given ID.
        /// </returns>
        /// <exception cref="ArgumentException">Thrown if the userId is invalid or null.</exception>
        public async Task<Users> GetUserProfileByIdAsync(int userId)
        {
            // Retrieves the user record from the database using the provided user ID
            return await _context.Users.FindAsync(userId);
        }

        #endregion

        #region Profile Update

        /// <summary>
        /// Updates the user profile in the database with the new information provided in the <paramref name="user"/> object.
        /// This method marks the user entity as updated and then persists the changes in the database.
        /// </summary>
        /// <param name="user">The <see cref="Users"/> object containing the updated user profile data.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <c>boolean</c> value indicating whether the update was successful.
        /// Returns <c>true</c> if the update was successful; otherwise, returns <c>false</c>.
        /// </returns>
        /// <exception cref="ArgumentException">Thrown if the provided <paramref name="user"/> is null or invalid.</exception>
        public async Task<bool> UpdateUserAsync(Users user)
        {
            // Mark the user as updated and save changes to the database
            _context.Users.Update(user);
            return await _context.SaveChangesAsync() > 0; // Returns true if any changes were saved
        }

        #endregion

        #region Password Change

        /// <summary>
        /// Changes the password of the user identified by their <paramref name="userId"/>.
        /// This method finds the user in the database, updates the password field, and saves the changes.
        /// </summary>
        /// <param name="userId">The unique identifier of the user whose password is to be changed.</param>
        /// <param name="newPassword">The new password that will replace the user's existing password.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <c>boolean</c> value indicating whether the password change was successful.
        /// Returns <c>true</c> if the password change was successful; otherwise, returns <c>false</c>.
        /// </returns>
        /// <exception cref="ArgumentException">Thrown if the user with the specified ID does not exist.</exception>
        public async Task<bool> ChangeUserPasswordAsync(int userId, string newPassword)
        {
            // Find the user with the provided userId
            var user = await _context.Users.FindAsync(userId);
            if (user != null)
            {
                // Update the user's password
                user.Password = newPassword;
                return await _context.SaveChangesAsync() > 0; // Return true if password is updated successfully
            }
            return false; // Return false if user was not found
        }

        #endregion

        #region Profile Retrieval by Email

        /// <summary>
        /// Retrieves the user profile by their email address.
        /// </summary>
        /// <param name="email">The email address of the user.</param>
        /// <returns>
        /// A task that represents the asynchronous operation. The task result is a <see cref="Users"/> object
        /// containing the user's profile data, or <c>null</c> if no user is found with the specified email.
        /// </returns>
        public async Task<Users> GetByEmailAsync(string email)
        {
            if (string.IsNullOrEmpty(email)) throw new ArgumentException("Email cannot be null or empty.", nameof(email));

            // Find the user by email in the database
            return await _context.Users
                                 .FirstOrDefaultAsync(u => u.Email.Equals(email, StringComparison.OrdinalIgnoreCase));
        }

        #endregion
    }
}
