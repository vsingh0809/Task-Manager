
#region namespace
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using Microsoft.AspNetCore.Identity;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.Extensions.Logging;
using TaskManager.Business.Interface;
using TaskManager.Models.Models;
using TaskManager.DataAccess.Interface;
using TaskManager.Entities.UserDTO;
using TaskManager.Entities.UsersDTO;
using TaskManager.Buisness.Interface;
#endregion

namespace TaskManager.Business.Class
{
    public class UsersService : IUsersService
    {
        private readonly IUsersRepository _userRepository;
        private readonly IPasswordHasher<Users> _passwordHasher;
        private readonly IConfiguration _configuration;
        private readonly ILogger<UsersService> _logger;
        private readonly ITokenService _tokenService;
        public UsersService(IUsersRepository userRepository, IPasswordHasher<Users> passwordHasher,
            IConfiguration configuration, ILogger<UsersService> logger,
            ITokenService tokenService)
        {
            _userRepository = userRepository;
            _passwordHasher = passwordHasher;
            _configuration = configuration;
            _logger = logger;
            _tokenService = tokenService;
        }

        #region LoginAsync
        public async Task<UserLoginResponseDto> LoginAsync(UserLoginRequestDTO userDTO)
        {
            Users? user = await _userRepository.GetUserByUsernameOrEmailAsync(userDTO);

            if (user == null)
            {
                throw new UnauthorizedAccessException("Invalid credentials.");
            }

            var result = _passwordHasher.VerifyHashedPassword(user, user.Password, userDTO.Password);

            if (result != PasswordVerificationResult.Success)
            {
                throw new UnauthorizedAccessException("Invalid credentials.");
            }

            var accessToken = _tokenService.GenerateJwtToken(user);
            var refreshToken = _tokenService.GenerateRefreshToken();

            await _userRepository.SaveRefreshTokenAsync(user.UserId, refreshToken);

            return new UserLoginResponseDto
            {
                AccessToken = accessToken,
                RefreshToken = refreshToken
            };
        }
        #endregion

        #region Refresh Token 
        public async Task<RefreshTokenResponseDTO> RefreshTokenAsync(RefreshTokenRequestDTO refreshTokenDTO)
        {
            // Validate the refresh token
            var user = await _userRepository.GetUserByRefreshTokenAsync(refreshTokenDTO.RefreshToken);
            if (user == null)
            {
                throw new UnauthorizedAccessException("Invalid refresh token.");
            }

            // Generate new access and refresh tokens
            var newAccessToken = _tokenService.GenerateJwtToken(user);
            var newRefreshToken = _tokenService.GenerateRefreshToken();

            // Save the new refresh token in the database
            await _userRepository.SaveRefreshTokenAsync(user.UserId, newRefreshToken);

            // Return the response
            return new RefreshTokenResponseDTO
            {
                AccessToken = newAccessToken,
                RefreshToken = newRefreshToken
            };
        } 
        #endregion

        #region Register 
        public async Task<Users> RegisterAsync(UserRegisterDTO userDTO)
        {
            if (userDTO == null)
            {
                _logger.LogError("User registration data is invalid. UserDTO is null.");
                throw new ArgumentException("User registration data is invalid.");
            }
            try
            {
                _logger.LogInformation("Checking if email {Email} already exists.", userDTO.Email);
                var existingEmail = _userRepository.EmailExists(userDTO.Email);
                if (existingEmail)
                {
                    _logger.LogWarning("Email {Email} already exists.", userDTO.Email);
                    throw new Exception("Email already exists.");
                }

               /* _logger.LogInformation("Checking if username {Username} already exists.", userDTO.UserName);
                var existingUsername = _userRepository.UsernameExists(userDTO.UserName);
                if (existingUsername)
                {
                    _logger.LogWarning("Username {Username} already exists.", userDTO.UserName);
                    throw new Exception("Username already exists.");
                }*/

                //Hash the password
               var user = new Users
                {
                    FirstName = userDTO.FirstName,
                    LastName = userDTO.LastName,
                    Username = userDTO.UserName,
                    Email = userDTO.Email,
                    Password = _passwordHasher.HashPassword(new Users(), userDTO.Password)
                };
                _logger.LogInformation("Registering user with username {Username} and email {Email}.", userDTO.UserName, userDTO.Email);
                var registeredUser = await _userRepository.RegisterUserAsync(user);
                _logger.LogInformation("User registered successfully with username {Username} and email {Email}.", userDTO.UserName, userDTO.Email);
                return registeredUser;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "An error occurred while registering the user with email {Email} and username {Username}.", userDTO.Email, userDTO.UserName);
                throw;
            }
        }
        #endregion

        #region Forgot Password
        public async Task<string> ForgotPasswordAsync(ForgotPasswordDTO forgotPasswordDTO)
        {
            if (forgotPasswordDTO == null || string.IsNullOrWhiteSpace(forgotPasswordDTO.Email) || string.IsNullOrWhiteSpace(forgotPasswordDTO.NewPassword))
            {
                _logger.LogWarning("Invalid password reset attempt. Email or new password is missing. Email: {Email}", forgotPasswordDTO?.Email);
                throw new ArgumentException("Email and new password are required.");
            }

            _logger.LogInformation("Initiating password reset for email: {Email}", forgotPasswordDTO.Email);
            var user = await _userRepository.GetUserByEmailAsync(forgotPasswordDTO.Email);

            if (user == null)
            {
                _logger.LogWarning("User with email {Email} not found during password reset attempt.", forgotPasswordDTO.Email);
                throw new Exception("User not found.");
            }

            // Hash the new password
            user.Password = _passwordHasher.HashPassword(user, forgotPasswordDTO.NewPassword);
            try
            {
                var result = await _userRepository.ForgotPasswordAsync(forgotPasswordDTO.Email, user.Password);
                _logger.LogInformation("Password reset successfully for user with email: {Email}", forgotPasswordDTO.Email);
                return result;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "An error occurred while resetting the password for email: {Email}", forgotPasswordDTO.Email);
                throw;
            }
        }
        #endregion

        #region Generate JWT token
        private string GenerateJwtToken(Users user)
        {
            var claims = new List<Claim>
    {
        new Claim(JwtRegisteredClaimNames.Sub, user.UserId.ToString())
    };

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration["JwtSettings:SecretKey"]));
            var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var tokenDescriptor = new JwtSecurityToken(
                issuer: _configuration["JwtSettings:Issuer"],
                audience: _configuration["JwtSettings:Audience"],
                expires: DateTime.Now.AddHours(24),  
                claims: claims,
                signingCredentials: credentials
            );

            var tokenHandler = new JwtSecurityTokenHandler();
            return tokenHandler.WriteToken(tokenDescriptor);
        }

        #endregion

        #region check Email Exits
        public bool EmailExistsService(string email)
        {
            return _userRepository.EmailExists(email);
        }
        #endregion

        #region check username exits
        public bool UsernameExistsService(string username)
        {
            return _userRepository.UsernameExists(username);
        }
        #endregion

        #region Generate username
        public string GenerateUsernameService(string firstName, string lastName)
        {
            return _userRepository.GenerateUsername(firstName, lastName);
        }
        #endregion

        #region Get users List:EF
        public async Task<List<Users>> GetAllUsersAsync()
        {
            return await _userRepository.GetAllUsersAsync();
        }
        #endregion

        #region Get users list:Stored procedure
        IEnumerable<Users> IUsersService.GetAllUsersByStoredProcedureAsync()
        {
            return _userRepository.GetAllUsersByStoredProcedureAsync();
        }
        #endregion

    }

}
