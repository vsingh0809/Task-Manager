
#region namespace
using TaskManager.Entities.UserDTO;
using TaskManager.Models.Models;
#endregion

namespace TaskManager.DataAccess.Interface
{
    public  interface IUsersRepository
    {
        Task<Users> GetUserByUsernameOrEmailAsync(UserLoginRequestDTO userDTO);
        Task SaveRefreshTokenAsync(int userId, string refreshToken);
        Task<Users> GetUserByRefreshTokenAsync(string refreshToken);
        IQueryable<Users> GetAllUsersByStoredProcedureAsync();
        Task<List<Users>> GetAllUsersAsync();
        Task<Users> RegisterUserAsync(Users user);
        Task<Users> GetUserByLoginAsync(string userNameOrEmail);
        Task<Users> GetUserByEmailAsync(string email);
        Task<Users> GetUserByUsernameAsync(string username);
        public string GenerateUsername(string firstName, string lastName);
        public bool UsernameExists(string username);
        public bool EmailExists(string email);
        Task<string> ForgotPasswordAsync(string email, string newPassword);
    }
}
