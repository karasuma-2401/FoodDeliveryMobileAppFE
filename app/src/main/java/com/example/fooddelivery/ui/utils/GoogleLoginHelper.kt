package com.example.fooddelivery.ui.utils

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.fooddelivery.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Composable
fun rememberGoogleLoginLauncher(
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

    return {
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                if (serverClientId.isEmpty()) {
                    onError("Google Client ID is missing. Add GOOGLE_WEB_CLIENT_ID to local.properties and rebuild.")
                    return@launch
                }

                val activityContext = context.findActivity()
                if (activityContext == null) {
                    Log.e("GoogleLogin", "Activity context not found for Credential Manager")
                    onError("Unable to start Google sign-in")
                    return@launch
                }

                val result = credentialManager.getCredential(
                    context = activityContext,
                    request = request
                )

                val idToken = extractGoogleIdToken(result.credential)
                if (idToken.isNullOrBlank()) {
                    onError("Google ID token is missing")
                } else {
                    onSuccess(idToken)
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d("GoogleLogin", "Login cancelled by user")
            } catch (e: GetCredentialException) {
                Log.e("GoogleLogin", "Credential error [${e.type}]: ${e.message}", e)
                onError(mapGoogleCredentialError(e))
            } catch (e: Exception) {
                Log.e("GoogleLogin", "Unexpected sign-in error: ${e.message}", e)
                onError("A security error occurred during login")
            }
        }
    }
}

private fun mapGoogleCredentialError(error: GetCredentialException): String {
    val detail = error.message.orEmpty()
    return when {
        error is NoCredentialException ||
            detail.contains("No credentials", ignoreCase = true) ->
            "No Google account found. Sign in to Google on this device and try again."

        detail.contains("Developer console", ignoreCase = true) ||
            detail.contains("10:", ignoreCase = false) ->
            "Google Sign-In is not configured for this app. Verify package name and SHA-1 in Google Cloud Console."

        detail.contains("reauth", ignoreCase = true) ||
            detail.contains("[16]", ignoreCase = false) ->
            "Google account verification failed. Check Web Client ID and SHA-1 fingerprint."

        BuildConfig.DEBUG && detail.isNotBlank() ->
            "Google sign-in failed: $detail"

        else -> "Authentication failed"
    }
}

private fun Context.findActivity(): ComponentActivity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}

private fun extractGoogleIdToken(credential: Credential): String? {
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        return try {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleLogin", "Invalid Google ID token response: ${e.message}")
            null
        }
    }
    Log.e("GoogleLogin", "Unexpected credential type: ${credential::class.simpleName}")
    return null
}

fun signOutGoogle(context: Context, scope: kotlinx.coroutines.CoroutineScope) {
    val credentialManager = CredentialManager.create(context)
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("GoogleLogin", "Secure sign out error: ${e.message}")
        }
    }
}
