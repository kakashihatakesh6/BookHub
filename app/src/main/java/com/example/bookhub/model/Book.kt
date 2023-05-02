package com.example.bookhub.model

import android.media.Image

data class Book(
    val bookId: String,
    val bookName: String,
    val bookAuthor: String,
    val bookRating: String,
    val bookPrice: String,
    val bookImage: String
) {
}