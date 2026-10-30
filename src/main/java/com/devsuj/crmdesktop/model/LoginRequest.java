package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code POST /api/auth/login}. */
public record LoginRequest(String email, String password) {
}
