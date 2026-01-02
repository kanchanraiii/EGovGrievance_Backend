package com.auth.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.auth.model.UserRole;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String role;
    private String departmentId;
    private Instant createdAt;

    // Default Constructor
    public User() {}

    // Manual Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // 1. The builder method MUST return UserBuilder, not Object
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    // 2. The Inner Builder Class
    public static class UserBuilder {
        private String email;
        private String password;
        private String fullName;
        private String phone;
        private String role = UserRole.CITIZEN.value();
        private String departmentId;
        private Instant createdAt;

        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder role(String role) { this.role = role; return this; }
        public UserBuilder departmentId(String departmentId) { this.departmentId = departmentId; return this; }
        public UserBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public User build() {
            User user = new User();
            user.setEmail(this.email);
            user.setPassword(this.password);
            user.setFullName(this.fullName);
            user.setPhone(this.phone);
            user.setRole(this.role);
            user.setDepartmentId(this.departmentId);
            user.setCreatedAt(this.createdAt);
            return user;
        }
    }
}
