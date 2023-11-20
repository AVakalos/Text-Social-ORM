package org.apostolis.comments.domain;

public record CreateCommentRequest(long post, String text) {
}
