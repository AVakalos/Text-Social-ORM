package org.apostolis.posts.application.ports.in;

import org.apostolis.common.PageRequest;

public record PostViewsQuery(long user, PageRequest pageRequest) { }
