package com.example.project.comment

data class CommentInfo(
    var comment: Comment,
    var commentId: String,
    var postId: String,
    var userLogin: String,
    var userPfpUrl: String?,
    var isEditable: Boolean
)
