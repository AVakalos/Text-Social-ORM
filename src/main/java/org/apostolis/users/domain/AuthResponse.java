package org.apostolis.users.domain;

public record AuthResponse(
    String username,
    String token
){ }