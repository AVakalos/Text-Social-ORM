package org.apostolis.comments.domain;

// Model request from controller
public record CreateCommentRequest(long post, String text) {
}
