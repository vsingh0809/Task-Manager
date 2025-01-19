using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using MySql.Data.MySqlClient;
using Serilog;

namespace TaskManager.Business.Class
{
    public class MySqlConnectionCheckService
    {
        private readonly string _connectionString;

        public MySqlConnectionCheckService(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection");
        }

        public async Task<bool> CheckConnectionAsync()
        {
            try
            {
                using var connection = new MySqlConnection(_connectionString);
                await connection.OpenAsync();
                return connection.State == System.Data.ConnectionState.Open;
            }
            catch (Exception ex)
            {
                //Log.Error("Error while checking MySQL connection: {ErrorMessage}", ex.Message);
                Log.Error("Error while checking MySQL connection: {ErrorMessage}, StackTrace: {StackTrace}", ex.Message, ex.StackTrace);
                return false;
            }
        }
    }
}
