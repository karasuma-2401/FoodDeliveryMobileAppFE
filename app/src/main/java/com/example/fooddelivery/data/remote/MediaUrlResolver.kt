package com.example.fooddelivery.data.remote

import com.example.fooddelivery.BuildConfig

object MediaUrlResolver {
    private const val EMULATOR_HOST = "10.0.2.2"

    fun resolve(path: String, apiBaseUrl: String = BuildConfig.API_BASE_URL): String {
        val absolute = when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("file://") || path.startsWith("content://") -> path
            else -> {
                val base = apiBaseUrl
                    .removeSuffix("/api/")
                    .removeSuffix("/api")
                    .trimEnd('/')
                if (path.startsWith("/")) "$base$path" else "$base/$path"
            }
        }
        return rewriteLocalhostForDevice(absolute)
    }

    /**
     * MinIO/API thường trả URL dạng http://localhost:9000/...
     * Trên Android emulator, localhost trỏ vào chính emulator — cần map sang 10.0.2.2 (host machine).
     */
    private fun rewriteLocalhostForDevice(url: String): String {
        if (url.startsWith("file://") || url.startsWith("content://")) return url
        if (!BuildConfig.DEBUG) return url
        return url
            .replace("http://localhost:", "http://$EMULATOR_HOST:")
            .replace("https://localhost:", "https://$EMULATOR_HOST:")
            .replace("http://127.0.0.1:", "http://$EMULATOR_HOST:")
            .replace("https://127.0.0.1:", "https://$EMULATOR_HOST:")
    }
}
