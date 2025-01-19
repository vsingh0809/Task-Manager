
#region namespace
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TaskManager.Models.Models; 
#endregion

namespace TaskManager.Buisness.Interface
{
    public interface ITokenService
    {
        string GenerateJwtToken(Users user);
        string GenerateRefreshToken();
    }
}
