package com.example.fooddelivery.ui.utils

import android.content.Context
import android.content.Intent

fun Context.shareText(
    text: String,
    subject: String? = null,
    chooserTitle: String = "Share"
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    }
    startActivity(Intent.createChooser(intent, chooserTitle))
}
