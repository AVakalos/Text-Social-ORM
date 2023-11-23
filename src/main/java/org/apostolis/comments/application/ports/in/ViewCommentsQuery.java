package org.apostolis.comments.application.ports.in;

import org.apostolis.common.PageRequest;

public record ViewCommentsQuery(long user, PageRequest pageRequest)  { }
