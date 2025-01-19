# Task-Manager
> A robust .NET Core-based task management solution with user authentication, profile management, and email notifications.

![.NET Core](https://img.shields.io/badge/.NET%20Core-8.0-blue)
![C#](https://img.shields.io/badge/C%23-10.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-9.1.0-orange)
![License](https://img.shields.io/badge/License-MIT-green)

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

## Overview
Task Management System is a comprehensive solution developed using .NET Core, providing functionalities for task management, user authentication, profile management, and email notifications. Developed by SKO trainee batch 2 Team B, this system implements best practices in software architecture and security.

## Features
### User Management
- User registration and authentication
- JWT-based authorization
- Profile management with photo upload
- Password management with secure hashing
- Email verification system

### Task Management
- Create, read, update, and delete tasks
- Task filtering and search capabilities
- Multi-select task operations
- Task status tracking
- Date-based task filtering

### Profile Management
- Profile photo upload (supported formats: JPG, JPEG, PNG)
- User information updates
- Password change functionality
- Profile data retrieval

### Email Services
- Email verification
- Password reset functionality
- OTP validation system

## Architecture
### Technology Stack
- **Backend**: .NET Core 8.0
- **Database**: MySQL 9.1.0
- **ORM**: Entity Framework Core
- **Authentication**: JWT Bearer tokens
- **Email Service**: SMTP Integration
- **Testing**: xUnit, Moq

### Project Structure
```plaintext
TaskManager/
├── TaskManager.API/            # API Controllers and Startup
├── TaskManager.Business/       # Business Logic Layer
│   ├── Interface/             # Service Interfaces
│   └── Implementation/        # Service Implementations
├── TaskManager.DataAccess/     # Data Access Layer
│   ├── Interface/           	# Data/Repository Interfaces
│   └── Implementation/        # Data/Repository Implementations
├── TaskManager.Common/        # Common for project
│   └── CustomExceptions/      # Common custom exceptions
├── TaskManager.Entities/      # Data Transfer Objects
│   ├── EmailDTO/            # Email-related DTOs
│   ├── ProfileDTO/          # Profile-related DTOs
│   ├── TaskDTO/             # Task-related DTOs
│   └── UserDTO/             # User-related DTOs
├── TaskManager.Models/        # Data Access Layer
│   ├── Data/           		 
│   └── Models/             
└── TaskManager.Tests/         # Unit Tests

### Prerequisites

    .NET Core SDK 8.0 or later
    MySQL Server 9.1.0 or later
    Visual Studio 2022 or VS Code
    Git

### Installation

    Clone the repository:

``bash

git clone https://skosystems.visualstudio.com/SKO.XploreWings.Learning/_git/SKO.XploreWings.Team-B-API

    Navigate to the project directory:

``bash

cd SKO.XploreWings.Learning/_git/SKO.XploreWings.Team-B-API

    Install dependencies:

``bash

dotnet restore

    Update database:

``bash

dotnet ef database update

    Run the application:

``bash

dotnet run --project TaskManager.API

### Configuration
Application Settings
JSON

{
  "ConnectionStrings": {
    "DefaultConnection": "Server=<server>;Database=<database>;User=<user>;Password=<password>;"
  },
  "JwtSettings": {
    "SecretKey": "<your-secret-key>",
    "Issuer": "<your-issuer>",
    "Audience": "<your-audience>",
    "ExpirationInMinutes": 60
  },
  "EmailSettings": {
    "SmtpServer": "<smtp-server>",
    "Port": 587,
    "UseSsl": true,
    "Username": "<email>",
    "Password": "<password>"
  }
}

### API Documentation
## Authentication Endpoints

    POST /api/User/register - Register new user
    POST /api/User/login - User login
    POST /api/User/forgot-password - Reset password
    POST /api/User/refresh-token - Refresh JWT token

## Task Endpoints

    GET /api/Tasks - Get all tasks
    POST /api/Tasks - Create new task
    PUT /api/Tasks/{id} - Update task
    DELETE /api/Tasks/{id} - Delete task
    GET /api/Tasks/search - Search tasks
    POST /api/Tasks/multi-delete - Delete multiple tasks

## Profile Endpoints

    GET /api/UserProfile - Get user profile
    PUT /api/UserProfile - Update profile
    POST /api/UserProfile/photo - Upload profile photo
    DELETE /api/UserProfile/photo - Remove profile photo
    POST /api/UserProfile/password - Change password

### Testing

The project includes comprehensive unit tests using xUnit and Moq:
``bash

dotnet test TaskManager.Tests

## Test Categories

    Controller Tests
    Service Layer Tests
    Repository Tests
    Integration Tests

### Security

    JWT Authentication
    Password Hashing
    Input Validation
    File Upload Validation
    CORS Policy
    Rate Limiting
    Request Validation

### Author

Ganesh Balaji Kirwale Patil

### Last Updated
2025-01-09 
