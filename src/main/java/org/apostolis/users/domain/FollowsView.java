package org.apostolis.users.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.common.PersistenseDataTypes.UsersById;

// View class for DTO Mapping
@AllArgsConstructor
@Getter
public class FollowsView {
    UsersById users;
}
