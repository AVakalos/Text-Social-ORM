package org.apostolis.users.domain;

import jakarta.validation.constraints.Positive;
import lombok.*;
import java.io.Serializable;

// Wrapper for User id

@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserId implements Serializable {

    @Positive
    private Long user_id;

    public Long getValue() {
        return user_id;
    }

    public String toString(){
        return "User"+user_id;
    }
}
