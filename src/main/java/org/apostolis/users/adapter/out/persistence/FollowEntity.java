package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name="followers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class FollowEntity {

    @EmbeddedId
    private FollowerEntityId id;
//    @Id
//    @Column(name="user_id")
//    private Long user_id;
//
//    @Id
//    @Column(name="following_id")
//    private Long following_id;


//    @Id
//    @JoinColumn(name="user_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private UserEntity user;
//
//    @Id
//    @JoinColumn(name="following_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private UserEntity following_user;
//
//    public FollowEntity(UserEntity user, UserEntity following_user){
//        this.user = user;
//        this.following_user = following_user;
//    }

//    public static FollowEntity mapToEntity(Follow follow){
//        return new FollowEntity(follow.user().getUser_id(),follow.followingId().getUser_id());
//    }
}
