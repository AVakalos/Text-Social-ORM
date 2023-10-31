package org.apostolis.users.domain;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    FREE,
    PREMIUM
}
