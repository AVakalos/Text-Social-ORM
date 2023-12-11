package org.apostolis.posts.application.ports.in;

import org.apostolis.common.PageRequest;
import org.apostolis.users.domain.UserId;

public record PostViewsQuery(UserId user, PageRequest pageRequest) { }
