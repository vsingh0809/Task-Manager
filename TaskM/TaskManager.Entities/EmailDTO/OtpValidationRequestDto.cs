﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TaskManager.Entities.EmailDTO
{
    public class OtpValidationRequestDto
    {
        public string Email { get; set; }
        public string Otp { get; set; }
    }
}
