package org.apostolis.common.PersistenseDataTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.users.domain.UserDetails;
import org.apostolis.users.domain.UserId;

import java.util.Map;

@AllArgsConstructor
@Getter
public class UsersById {
    private Map<UserId, UserDetails> data;
}
