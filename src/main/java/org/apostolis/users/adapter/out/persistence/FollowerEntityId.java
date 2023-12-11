package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
public class FollowerEntityId implements Serializable {
    @Column(name="user_id")
    private Long user_id;
    @Column(name="following_id")
    private Long following_id;
}
