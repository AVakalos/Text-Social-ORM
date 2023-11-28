package org.apostolis.posts.application.ports.in;

import org.apostolis.common.PageRequest;
import org.apostolis.users.adapter.out.persistence.UserId;

public record PostViewsQuery(UserId user, PageRequest pageRequest) { }
