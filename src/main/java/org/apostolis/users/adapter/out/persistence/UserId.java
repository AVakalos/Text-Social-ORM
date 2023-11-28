package org.apostolis.users.adapter.out.persistence;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserId implements Serializable {

    private Long user_id;
    public String toString(){
        return "User"+user_id;
    }
}
