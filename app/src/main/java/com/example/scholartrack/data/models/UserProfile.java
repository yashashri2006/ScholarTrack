package com.example.scholartrack.data.models;

// Unit 5: Data model for a Firebase user profile node

public class UserProfile {
    public String name;
    public String email;
    public String role;
    public String rollNo;
    public String className;
    public String branch;
    public String category;

    // Required by Firebase deserialisation
    public UserProfile() {}

    public UserProfile(String name, String email, String role,
                       String rollNo, String className, String branch) {
        this.name      = name;
        this.email     = email;
        this.role      = role;
        this.rollNo    = rollNo;
        this.className = className;
        this.branch    = branch;
        this.category  = "";
    }
}
