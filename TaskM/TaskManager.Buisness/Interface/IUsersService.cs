
#region namespace
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TaskManager.Entities.UserDTO;
using TaskManager.Entities.UsersDTO;
using TaskManager.Models.Models;
#endregion

namespace TaskManager.Business.Interface
{
    public interface IUsersService
    {
        Task<RefreshTokenResponseDTO> RefreshTokenAsync(RefreshTokenRequestDTO refreshTokenDTO);
        Task<List<Users>> GetAllUsersAsync();
        IEnumerable<Users> GetAllUsersByStoredProcedureAsync();
        Task<Users> RegisterAsync(UserRegisterDTO userDTO);
        Task<UserLoginResponseDto> LoginAsync(UserLoginRequestDTO userDTO);
        public bool UsernameExistsService(string username);
        public bool EmailExistsService(string email);
        public string GenerateUsernameService(string firstName, string lastName);
        Task<string> ForgotPasswordAsync(ForgotPasswordDTO forgotPasswordDTO);

    }
}
