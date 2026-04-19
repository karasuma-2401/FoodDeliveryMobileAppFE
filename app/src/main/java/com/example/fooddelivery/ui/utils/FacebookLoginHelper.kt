package com.example.fooddelivery.ui.utils

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult


@Composable
fun rememberFacebookLoginLauncher(
    onSuccess: (String) -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val callbackManager = remember { CallbackManager.Factory.create() }
    val facebookLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract(callbackManager, null)
    ) { result ->
    }
    DisposableEffect(Unit) {
        val callback = object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                onSuccess(result.accessToken.token)
            }
            override fun onCancel() {
                onCancel()
            }
            override fun onError(error: FacebookException) {
                onError(error.message ?: "Error from Facebook")
            }
        }
        LoginManager.getInstance().registerCallback(callbackManager, callback)

        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }
    return {
        facebookLauncher.launch(listOf("email", "public_profile"))
    }

}