package org.apostolis.comments.application.ports.in;

import org.apostolis.common.PageRequest;
import org.apostolis.users.adapter.out.persistence.UserId;

public record ViewCommentsQuery(UserId user_id, PageRequest pageRequest)  { }
