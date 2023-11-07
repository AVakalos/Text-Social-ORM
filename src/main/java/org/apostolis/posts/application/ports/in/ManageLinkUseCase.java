package org.apostolis.posts.application.ports.in;

import java.util.List;

public interface ManageLinkUseCase {
    String createLink(CreateLinkCommand createLinkCommand);
    List<Object> decodeLink(String url);
}
