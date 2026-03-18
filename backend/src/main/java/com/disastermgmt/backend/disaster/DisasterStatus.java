package com.disastermgmt.backend.disaster;

public enum DisasterStatus {
    PENDING,    // Awaiting admin verification
    ACTIVE,     // Verified and currently ongoing
    RESOLVED,   // Disaster has ended/resolved
    CANCELLED   // False alarm or cancelled alert
}
