package com.society.management.entity;

public enum Role {
    SUPER_ADMIN,
    SOCIETY_ADMIN,
    ACCOUNTANT,
    COMMITTEE,
    OWNER,
    TENANT,
    SECURITY;

    public String authority() { return "ROLE_" + name(); }
}
