package org.apostolis.posts.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apostolis.AppConfig;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.UserId;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@EqualsAndHashCode(exclude = {"post_comments"})
public class Post{
    private PostId id;
    private final UserId user;
    private final String text;
    private final LocalDateTime createdAt;

    private Set<Comment> post_comments = new HashSet<>();

    private Post(UserId user, String text){
        this.user = user;
        this.text = text;
        this.createdAt = LocalDateTime.now(AppConfig.getClock());
    }

    public Post(PostId id, UserId user, String text, LocalDateTime createdAt){
        this.id = id;
        this.user = user;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Post(PostId id, UserId user, String text, LocalDateTime createdAt, Set<Comment> comments){
        this.id = id;
        this.user = user;
        this.text = text;
        this.createdAt = createdAt;
        this.post_comments = comments;
    }



    public static Post createPost(UserId user, String text, Role role){
        long post_size = text.length();
        switch (role){
            case FREE:
                int free_post_size = AppConfig.getFREE_POST_SIZE();
                if(post_size > free_post_size){
                    throw new PostCreationException("Free users can post texts up to "+ free_post_size +" characters."+
                            "\nYour post was "+post_size+" characters");
                }
                break;
            case PREMIUM:
                int premium_post_size = AppConfig.getPREMIUM_POST_SIZE();
                if(post_size > premium_post_size){
                    throw new PostCreationException("Premium users can post texts up to "+ premium_post_size +" characters."+
                            "\nYour post was "+post_size+" characters");
                }
                break;
            default:
                break;
        }
        return new Post(user, text);
    }

    public void addComment(Comment comment,long comments_count, Role role){
        if(role.equals(Role.FREE)){
            int max_comments_number = AppConfig.getFREE_MAX_COMMENTS();
            if(comments_count >= max_comments_number){
                throw new CommentCreationException("Free users can comment up to "+ max_comments_number +" times per post."+
                        "\nYou reached the maximum number of comments for this post.");
            }
        }
        post_comments.add(comment);
    }
}
