package com.example.fooddelivery.ui.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.fooddelivery.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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

    // SECURITY ENHANCEMENT: Lấy Client ID từ BuildConfig thay vì hardcode
    val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

    return {
        // SECURITY ENHANCEMENT: Nonce logic để đảm bảo tính duy nhất của yêu cầu
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
                    onError("Google Client ID is missing in configuration")
                    return@launch
                }
                
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    onSuccess(credential.idToken)
                } else {
                    onError("Unexpected security credential type")
                }
            } catch (e: GetCredentialException) {
                if (e is GetCredentialCancellationException) {
                    Log.d("GoogleLogin", "Login cancelled by user")
                } else {
                    Log.e("GoogleLogin", "Security Error: ${e.message}")
                    onError("Authentication failed")
                }
            } catch (e: Exception) {
                onError("A security error occurred during login")
            }
        }
    }
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
