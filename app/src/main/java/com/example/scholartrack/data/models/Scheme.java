package com.example.scholartrack.data.models;

// Unit 5: Data model for a scheme parsed from assets/schemes.json (Unit 6)

import java.util.List;

public class Scheme {
    public String id;
    public String name;
    public String department;
    public String benefit;
    public List<String> categories;
    public long maxIncome;       // 0 = no income limit
    public boolean femaleOnly;
    public boolean hostelOnly;
    public boolean pwdOnly;
    public boolean minorityOnly;
    public boolean verify;       // if true, show "Confirm rules on portal" chip
    public List<String> documents;

    public Scheme() {}
}
