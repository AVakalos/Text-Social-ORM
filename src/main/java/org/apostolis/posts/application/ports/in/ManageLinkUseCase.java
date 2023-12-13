package org.apostolis.posts.application.ports.in;

import org.apostolis.posts.domain.PostsWithNLatestCommentsView;

public interface ManageLinkUseCase {
    String createLink(CreateLinkCommand createLinkCommand) throws Exception;
    PostsWithNLatestCommentsView decodeLink(String url) throws Exception;
}
