package com.example.scholartrack.data.models;

// Unit 5: Data model for a single application stage node in Firebase

import java.util.HashMap;
import java.util.Map;

public class StageInfo {
    public boolean done;
    public String date;
    public String note;
    public String updatedBy;

    public StageInfo() {}

    public StageInfo(boolean done, String date, String note, String updatedBy) {
        this.done      = done;
        this.date      = date;
        this.note      = note;
        this.updatedBy = updatedBy;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("done", done);
        map.put("date", date);
        map.put("note", note);
        map.put("updatedBy", updatedBy);
        return map;
    }
}
