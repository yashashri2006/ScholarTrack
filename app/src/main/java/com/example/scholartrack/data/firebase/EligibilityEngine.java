package com.example.scholartrack.data.firebase;

// Unit 6: Eligibility engine – filters schemes by category, income and special conditions.

import com.example.scholartrack.data.models.Scheme;

import java.util.ArrayList;
import java.util.List;

public class EligibilityEngine {

    /**
     * Returns the subset of all schemes that match the student's profile.
     *
     * @param allSchemes  Full list from SchemeRepository.
     * @param category    One of: SC, ST, OBC, VJNT, SBC, "Open / EBC"
     * @param income      Annual family income in rupees (0 means unknown – skip income filter).
     * @param isFemale    true if gender is Female.
     * @param isMinority  true if minority community.
     * @param isPwd       true if person with disability.
     * @param isHostel    true if hostel resident.
     */
    public List<Scheme> filter(List<Scheme> allSchemes,
                               String category,
                               long income,
                               boolean isFemale,
                               boolean isMinority,
                               boolean isPwd,
                               boolean isHostel) {
        List<Scheme> result = new ArrayList<>();

        for (Scheme s : allSchemes) {
            // ── Special-condition-only schemes ──────────────────────────
            if (s.femaleOnly  && !isFemale)    continue;
            if (s.hostelOnly  && !isHostel)    continue;
            if (s.pwdOnly     && !isPwd)       continue;
            if (s.minorityOnly && !isMinority) continue;

            // ── Category match ───────────────────────────────────────────
            if (!s.categories.isEmpty() && !s.categories.contains(category)) continue;

            // ── Income cap (0 means no limit stored for that scheme) ─────
            if (s.maxIncome > 0 && income > 0 && income > s.maxIncome) continue;

            result.add(s);
        }

        return result;
    }
}
