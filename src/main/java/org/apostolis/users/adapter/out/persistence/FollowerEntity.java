package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name="followers")
@NoArgsConstructor
@AllArgsConstructor
public class FollowerEntity {

    @EmbeddedId
    private FollowerEntityId id;
}
