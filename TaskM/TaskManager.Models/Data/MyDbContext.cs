using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using TaskManager.Models.Models;

namespace TaskManager.Models.Data;

public partial class MyDbContext : DbContext
{
    public MyDbContext()
    {
    }

    public MyDbContext(DbContextOptions<MyDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<OtpTempTable> OtpTempTables { get; set; }

    public virtual DbSet<Tasks> Tasks { get; set; }

    public virtual DbSet<TasksBackup> TasksBackups { get; set; }

    public virtual DbSet<Users> Users { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseMySQL("Server=3.231.99.201;Port=3306;Database=team_b;User=user3;Password=B3teamSKO;");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<OtpTempTable>(entity =>
        {
            entity.HasKey(e => e.OtpId).HasName("PRIMARY");

            entity.ToTable("otp_temp_table");

            entity.Property(e => e.OtpId).HasColumnName("otp_id");
            entity.Property(e => e.CreationDate)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("creation_date");
            entity.Property(e => e.Email)
                .HasMaxLength(255)
                .HasColumnName("email");
            entity.Property(e => e.GeneratedOtp).HasColumnName("generated_otp");
            entity.Property(e => e.IsUsed)
                .HasMaxLength(1)
                .HasDefaultValueSql("'N'")
                .IsFixedLength()
                .HasColumnName("is_used");
        });

        modelBuilder.Entity<Tasks>(entity =>
        {
            entity.HasKey(e => e.TaskId).HasName("PRIMARY");

            entity.ToTable("tasks");

            entity.HasIndex(e => e.UserId, "user_id");

            entity.Property(e => e.TaskId).HasColumnName("task_id");
            entity.Property(e => e.CreatedBy)
                .HasMaxLength(30)
                .HasColumnName("created_by");
            entity.Property(e => e.CreationDate)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("creation_date");
            entity.Property(e => e.Description)
                .HasMaxLength(4000)
                .HasColumnName("description");
            entity.Property(e => e.EnabledFlag)
                .HasMaxLength(1)
                .HasDefaultValueSql("'Y'")
                .IsFixedLength()
                .HasColumnName("enabled_flag");
            entity.Property(e => e.EndDate)
                .HasColumnType("datetime")
                .HasColumnName("end_date");
            entity.Property(e => e.LastUpdatedBy)
                .HasMaxLength(30)
                .HasColumnName("last_updated_by");
            entity.Property(e => e.LastUpdationDate)
                .ValueGeneratedOnAddOrUpdate()
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("last_updation_date");
            entity.Property(e => e.StartDate)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("start_date");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .HasColumnName("status");
            entity.Property(e => e.Title)
                .HasMaxLength(255)
                .HasColumnName("title");
            entity.Property(e => e.UserId).HasColumnName("user_id");

            entity.HasOne(d => d.User).WithMany(p => p.Tasks)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("tasks_ibfk_1");
        });

        modelBuilder.Entity<TasksBackup>(entity =>
        {
            entity.HasKey(e => e.TaskId).HasName("PRIMARY");

            entity.ToTable("tasks_backup");

            entity.Property(e => e.TaskId).HasColumnName("task_id");
            entity.Property(e => e.BackedupOn)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("backedup_on");
            entity.Property(e => e.CreatedBy)
                .HasMaxLength(30)
                .HasColumnName("created_by");
            entity.Property(e => e.EndDate)
                .HasColumnType("datetime")
                .HasColumnName("end_date");
            entity.Property(e => e.StartDate)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("start_date");
            entity.Property(e => e.TaskDescription)
                .HasMaxLength(4000)
                .HasColumnName("task_description");
            entity.Property(e => e.TaskStatus)
                .HasMaxLength(20)
                .HasColumnName("task_status");
            entity.Property(e => e.TaskTitle)
                .HasMaxLength(255)
                .HasColumnName("task_title");
        });

        modelBuilder.Entity<Users>(entity =>
        {
            entity.HasKey(e => e.UserId).HasName("PRIMARY");

            entity.ToTable("users");

            entity.HasIndex(e => e.Email, "email").IsUnique();

            entity.Property(e => e.UserId).HasColumnName("user_id");
            entity.Property(e => e.CreatedBy)
                .HasMaxLength(30)
                .HasColumnName("created_by");
            entity.Property(e => e.CreationDate)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("creation_date");
            entity.Property(e => e.Email)
                .HasMaxLength(150)
                .HasColumnName("email");
            entity.Property(e => e.EnabledFlag)
                .HasMaxLength(1)
                .HasDefaultValueSql("'Y'")
                .IsFixedLength()
                .HasColumnName("enabled_flag");
            entity.Property(e => e.FirstName)
                .HasMaxLength(100)
                .HasColumnName("first_name");
            entity.Property(e => e.Image)
                .HasColumnType("blob")
                .HasColumnName("image");
            entity.Property(e => e.LastName)
                .HasMaxLength(100)
                .HasColumnName("last_name");
            entity.Property(e => e.LastUpdatedBy)
                .HasMaxLength(30)
                .HasColumnName("last_updated_by");
            entity.Property(e => e.LastUpdatedDate)
                .ValueGeneratedOnAddOrUpdate()
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime")
                .HasColumnName("last_updated_date");
            entity.Property(e => e.Password)
                .HasMaxLength(255)
                .HasColumnName("password");
            entity.Property(e => e.RefreshToken)
                .HasMaxLength(255)
                .HasColumnName("refresh_token");
            entity.Property(e => e.Username)
                .HasMaxLength(30)
                .HasColumnName("username");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
