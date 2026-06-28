package com.example.fooddelivery.data.remote

import com.example.fooddelivery.BuildConfig

object MediaUrlResolver {
    private fun devHost(): String = try {
        java.net.URI(BuildConfig.API_BASE_URL).host ?: "10.0.2.2"
    } catch (_: Exception) {
        "10.0.2.2"
    }

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
     * Map localhost sang host đang dùng trong API_BASE_URL (10.0.2.2 emulator / 127.0.0.1 USB / IP LAN).
     */
    private fun rewriteLocalhostForDevice(url: String): String {
        if (url.startsWith("file://") || url.startsWith("content://")) return url
        if (!BuildConfig.DEBUG) return url
        val host = devHost()
        return url
            .replace("http://localhost:", "http://$host:")
            .replace("https://localhost:", "https://$host:")
            .replace("http://127.0.0.1:", "http://$host:")
            .replace("https://127.0.0.1:", "https://$host:")
    }
}
