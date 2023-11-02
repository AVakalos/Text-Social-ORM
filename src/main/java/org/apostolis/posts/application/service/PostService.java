package org.apostolis.posts.application.service;

import org.apostolis.App;
import org.apostolis.AppConfig;
import org.apostolis.posts.application.ports.in.CreatePostCommand;
import org.apostolis.posts.application.ports.in.CreatePostUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostCreationException;
import org.apostolis.users.domain.Role;

public class PostService implements CreatePostUseCase {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void createPost(CreatePostCommand createPostCommand) {
        String text = createPostCommand.text();
        Role role = Role.valueOf(createPostCommand.role());

        int post_size = text.length();
        switch (role){
            case FREE:
                int free_post_size = AppConfig.getFreePostSize();
                if(post_size > free_post_size){
                    throw new PostCreationException("Free users can post texts up to "+ free_post_size +" characters."+
                            "\nYour post was "+post_size+" characters");
                }
                break;
            case PREMIUM:
                int premium_post_size = AppConfig.getPremiumPostSize();
                if(post_size > premium_post_size){
                    throw new PostCreationException("Premium users can post texts up to "+ premium_post_size +" characters."+
                            "\nYour post was "+post_size+" characters");
                }
                break;
            default:
                break;
        }
        Post postToSave = new Post(App.currentUserId.get(),text);
        postRepository.savePost(postToSave);
    }
}
