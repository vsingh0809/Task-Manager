        
#region namespace
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using TaskManager.DataAccess.Interface;
using TaskManager.Entities.UserDTO;
using TaskManager.Models.Data;
using TaskManager.Models.Models;
#endregion

namespace TaskManager.DataAccess.Class
{
    public class UsersRepository : IUsersRepository
    {
        private readonly ILogger<UsersRepository> _logger;
        private readonly MyDbContext _context;

        public UsersRepository(MyDbContext context, ILogger<UsersRepository> logger)
        {
            _context = context;
            _logger = logger;
        }

        #region Register user
        public async Task<Users> RegisterUserAsync(Users user)
        {
            try
            {
                user.CreatedBy = user.Username;
                user.LastUpdatedBy = user.Username;
                _logger.LogInformation("Registering user with email:{Email}", user.Email);
                await _context.Users.AddAsync(user);

                await _context.SaveChangesAsync();
                _logger.LogInformation("USer registered succesfully with id:{UserID}", user.UserId);
                return user;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to register user with email: {Email}", user.Email);
                //throw new DatabaseOperationException("Failed to register user with email", ex);
                throw new Exception("Registration failed.", ex);
            }
        }
        #endregion

        #region Login User
        public async Task<Users> GetUserByLoginAsync(string userNameOrEmail)
        {
            try
            {
                _logger.LogInformation("Fetching user by login: {Login}", userNameOrEmail);
                var user = await _context.Users
                    .FirstOrDefaultAsync(u => u.Username == userNameOrEmail || u.Email == userNameOrEmail);
                if (user == null)
                {
                    _logger.LogWarning("No user found for login: {Login}", userNameOrEmail);
                }
                return user;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to fetch user by login: {Login}", userNameOrEmail);
                //throw new DatabaseOperationException("An error occurred while fetching the user.", ex);
                throw new Exception("An error occurred while fetching the user.", ex);
            }
        }
        #endregion

        #region Token Management (New methods)
        public async Task SaveRefreshTokenAsync(int userId, string refreshToken)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user != null)
            {
                user.RefreshToken = refreshToken;
                await _context.SaveChangesAsync(); 
            }
        }

        public async Task<Users> GetUserByUsernameOrEmailAsync(UserLoginRequestDTO userDTO)
        {
            try
            {
                // Check if either the username or email is provided
                if (string.IsNullOrWhiteSpace(userDTO.UserName) && string.IsNullOrWhiteSpace(userDTO.Email))
                {
                    throw new ArgumentException("Either UserName or Email must be provided.");
                }

                // Query the database to find the user by either username or email
                Users user = await _context.Users
                    .FirstOrDefaultAsync(u => u.Username == userDTO.UserName || u.Email == userDTO.Email);

                if (user == null)
                {
                    // Log if no user is found
                    _logger.LogWarning("User not found for either username or email: {UserName}/{Email}", userDTO.UserName, userDTO.Email);
                }

                return user;  // Return the found user or null
            }
            catch (Exception ex)
            {
                // Log any exception that occurs during the operation
                _logger.LogError(ex, "An error occurred while fetching user by username or email.");
                throw new Exception("An error occurred while fetching user by username or email.", ex);
            }
        }

        public async Task<Users> GetUserByRefreshTokenAsync(string refreshToken)
        {
            return await _context.Users
                .FirstOrDefaultAsync(u => u.RefreshToken == refreshToken);
        }

        #endregion

        #region Find User by Email
        public async Task<Users> GetUserByEmailAsync(string email)
        {
            try
            {
                _logger.LogInformation("Getting user by email: {Email}", email);
                var user = await _context.Users.SingleOrDefaultAsync(u => u.Email == email);
                if (user == null)
                    //throw new UserNotFoundException(email);
                    throw new Exception("User not found");
                return user;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Fialed to find user by Email");
                throw new Exception("Error occured while fetcting user by email", ex);
                //throw new DatabaseOperationException("Error occured while fetcting user by email", ex);
            }
        }
        #endregion

        #region Find user by Username
        public async Task<Users> GetUserByUsernameAsync(string username)
        {
            try
            {
                _logger.LogInformation("Getting user by username: {Username}", username);
                var user = await _context.Users.SingleOrDefaultAsync(u => u.Username == username);
                if (user == null)
                    //throw new UserNotFoundException(username);
                    throw new Exception("USer not fuound");
                return user;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Fialed to find user by username");
               // throw new DatabaseOperationException("Error occured while fetcting user by username", ex);
                throw new Exception("Error occured while fetcting user by username", ex);

            }
        }
        #endregion

        #region Forgot Password
        public async Task<string> ForgotPasswordAsync(string email, string newPassword)
        {
            try
            {
                var user = await GetUserByEmailAsync(email);

                user.Password = newPassword;

                _context.Users.Update(user);

                await _context.SaveChangesAsync();
                _logger.LogInformation("Password reset succesfully");
                return "Password reset successfully.";
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to reset password for email: {Email}", email);
                // new DatabaseOperationException("An error occurred while resetting the password.", ex);
                throw new Exception("An error occurred while resetting the password.", ex);
            }

        }
        #endregion

        #region Generate Username
        public string GenerateUsername(string firstName, string lastName)
        {
            string username = $"{firstName.ToLower()}.{lastName.ToLower()}";
            if (UsernameExists(username))
            {
                int counter = 1;
                string newUsername = username + counter;
                while (UsernameExists(newUsername))
                {
                    counter++;
                    newUsername = username + counter;
                }
                return newUsername;
            }
            return username;
        }
        #endregion

        #region check Username Exists
        public bool UsernameExists(string username)
        {
            try
            {
                return _context.Users.Any(user => user.Username == username);
            }
            catch (Exception ex)
            {
                 _logger.LogError(ex, "Failed to find given username in database");
                 //throw new DatabaseOperationException("Falied while finding usernme in databse", ex);
                throw new Exception("Falied while finding usernme in databse", ex);
            }
        }
        #endregion

        #region check email exists
        public bool EmailExists(string email)
        {
            try
            {
                return _context.Users.Any(user => user.Email == email);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to find given email in database");
                //throw new DatabaseOperationException("Falied whilefinding email in databse", ex);
                throw new Exception("Falied whilefinding email in databse", ex);
            }
        }
        #endregion

        #region Get users list:EF
        public async Task<List<Users>> GetAllUsersAsync()
        {
            try
            {
                _logger.LogInformation("Fetching all users from the database.");
                var users = await _context.Users.ToListAsync();
                _logger.LogInformation("Successfully fetched {Count} users from the database.", users.Count);
                return users;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to fetch all users from the database.");
                //throw new DatabaseOperationException("An error occurred while fetching users.", ex);
                throw new Exception("An error occurred while fetching users.", ex);
            }
        }
        #endregion

        #region Get users list :Stored Procedure
        public IQueryable<Users> GetAllUsersByStoredProcedureAsync()
        {
            try
            {

                var users = _context.Users
                    .FromSqlRaw("CALL get_all_users()");

                return users;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to fetch all users from the database.");
                //throw new DatabaseOperationException("An error occurred while fetching users.", ex);
                throw new Exception("An error occurred while fetching users.", ex);

            }
        }
        #endregion
    }

}
