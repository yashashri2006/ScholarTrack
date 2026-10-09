package com.example.scholartrack.data.firebase;

// Unit 6: JSON parsing with JSONObject and JSONArray.
//         Parses assets/schemes.json into Scheme model objects.

import android.content.Context;

import com.example.scholartrack.data.models.Scheme;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SchemeRepository {

    private final List<Scheme> schemes = new ArrayList<>();
    private String compiledOn = "";
    private List<String> commonDocuments = new ArrayList<>();

    public SchemeRepository(Context context) {
        parse(context);
    }

    private void parse(Context context) {
        try {
            InputStream is = context.getAssets().open("schemes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject root = new JSONObject(json);
            compiledOn = root.optString("compiledOn", "");

            // ── Common documents ─────────────────────────────────────────
            JSONArray commonArr = root.optJSONArray("commonDocuments");
            if (commonArr != null) {
                for (int i = 0; i < commonArr.length(); i++) {
                    commonDocuments.add(commonArr.getString(i));
                }
            }

            // ── Schemes ──────────────────────────────────────────────────
            JSONArray schemesArr = root.getJSONArray("schemes");
            for (int i = 0; i < schemesArr.length(); i++) {
                JSONObject obj = schemesArr.getJSONObject(i);
                Scheme s = new Scheme();
                s.id           = obj.optString("id", "s" + i);
                s.name         = obj.optString("name", "");
                s.department   = obj.optString("department", "");
                s.benefit      = obj.optString("benefit", "");
                s.maxIncome    = obj.optLong("maxIncome", 0);
                s.femaleOnly   = obj.optBoolean("femaleOnly", false);
                s.hostelOnly   = obj.optBoolean("hostelOnly", false);
                s.pwdOnly      = obj.optBoolean("pwdOnly", false);
                s.minorityOnly = obj.optBoolean("minorityOnly", false);
                s.verify       = obj.optBoolean("verify", false);

                // categories
                JSONArray catArr = obj.optJSONArray("categories");
                s.categories = new ArrayList<>();
                if (catArr != null) {
                    for (int j = 0; j < catArr.length(); j++) {
                        s.categories.add(catArr.getString(j));
                    }
                }

                // scheme-specific documents
                JSONArray docsArr = obj.optJSONArray("documents");
                s.documents = new ArrayList<>();
                if (docsArr != null) {
                    for (int j = 0; j < docsArr.length(); j++) {
                        s.documents.add(docsArr.getString(j));
                    }
                }
                // Prepend common documents to each scheme's doc list
                s.documents.addAll(0, commonDocuments);

                schemes.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Scheme> getAll() { return schemes; }
    public String getCompiledOn() { return compiledOn; }
    public List<String> getCommonDocuments() { return commonDocuments; }
}
