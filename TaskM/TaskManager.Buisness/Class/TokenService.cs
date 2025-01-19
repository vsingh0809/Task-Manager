using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using TaskManager.Buisness.Interface;
using TaskManager.Models.Models;

public class TokenService : ITokenService
{
    private readonly IConfiguration _configuration;
    private readonly string _jwtSecret;
    private readonly string _jwtIssuer;
    private readonly string _jwtAudience;

    public TokenService(IConfiguration configuration)
    {
        _configuration = configuration;
        _jwtSecret = configuration["JwtSettings:SecretKey"];
        _jwtIssuer = configuration["JwtSettings:Issuer"];
        _jwtAudience = configuration["JwtSettings:Audience"];

        if (string.IsNullOrEmpty(_jwtSecret))
        {
            throw new ArgumentNullException("JwtSettings:SecretKey", "JWT Secret is not configured.");
        }
    }

    #region Generates the Access Token (JWT)
    public string GenerateJwtToken(Users user)
    {
        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub, user.UserId.ToString()),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new Claim(ClaimTypes.Name, user.Username),
        };

        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_jwtSecret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var token = new JwtSecurityToken(
            _jwtIssuer,
            _jwtAudience,
            claims,
            expires: DateTime.Now.AddHours(24),
            signingCredentials: creds
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
    #endregion

    #region Generates the Refresh Token (longer-lived token)

    public string GenerateRefreshToken()
    {
        return Guid.NewGuid().ToString();
    } 
    #endregion
}
