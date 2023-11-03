package org.apostolis.users.application.ports.out;

import org.apostolis.exception.DatabaseException;

import java.util.ArrayList;
import java.util.HashMap;

public interface FollowsRepository {
    void saveFollow(int user) throws DatabaseException;
    void deleteFollow(int user) throws DatabaseException;

    HashMap<Integer,String> getFollowers(int user) throws DatabaseException;
    HashMap<Integer,String> getFollowing(int user) throws DatabaseException;
    HashMap<Integer,String> getUsersToFollow(int user) throws DatabaseException;
}
