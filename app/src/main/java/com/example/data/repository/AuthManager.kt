package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthStatus(
    val userEmail: String? = null,
    val userName: String? = null,
    val userPhotoUrl: String? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthManager(private val context: Context) {
    private var _auth: FirebaseAuth? = null
    private val auth: FirebaseAuth?
        get() {
            if (_auth == null) {
                try {
                    _auth = FirebaseAuth.getInstance()
                } catch (e: Exception) {
                    Log.e("AuthManager", "Firebase not initialized", e)
                }
            }
            return _auth
        }
    private val credentialManager = CredentialManager.create(context)

    private val _authStatus = MutableStateFlow(AuthStatus())
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    init {
        // Observe current user safely
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _authStatus.value = AuthStatus(
                    userEmail = user.email,
                    userName = user.displayName,
                    userPhotoUrl = user.photoUrl?.toString(),
                    isAuthenticated = true
                )
            } else {
                _authStatus.value = AuthStatus(isAuthenticated = false)
            }
        }
    }

    suspend fun signInWithGoogle() {
        val currentAuth = auth
        if (currentAuth == null) {
            _authStatus.value = _authStatus.value.copy(
                errorMessage = "سرویس‌های گوگل در حال حاضر در دسترس نیستند (نیاز به google-services.json)."
            )
            return
        }

        val clientId = "YOUR_WEB_CLIENT_ID_FROM_FIREBASE_CONSOLE" // Replace with real ID
        if (clientId.startsWith("YOUR_WEB_CLIENT_ID")) {
            _authStatus.value = _authStatus.value.copy(
                errorMessage = "خطا: کلاینت آی‌دی گوگل تنظیم نشده است. لطفاً از کنسول فایربیس دریافت کنید."
            )
            return
        }

        _authStatus.value = _authStatus.value.copy(isLoading = true, errorMessage = null)
        try {
            // NOTE: In a real app, you'd get the web client ID from strings.xml or BuildConfig
            // Here we use a placeholder or assume the user will configure it via Secrets/Console.
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("YOUR_WEB_CLIENT_ID_FROM_FIREBASE_CONSOLE") 
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleSignIn(result)
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In failed", e)
            _authStatus.value = _authStatus.value.copy(
                isLoading = false,
                errorMessage = "خطا در ورود با گوگل: ${e.localizedMessage}"
            )
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val googleIdTokenCredential = credential
            val idToken = googleIdTokenCredential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            
            try {
                auth?.signInWithCredential(firebaseCredential)?.await()
                // AuthStateListener will update the status
            } catch (e: Exception) {
                _authStatus.value = _authStatus.value.copy(
                    isLoading = false,
                    errorMessage = "خطا در تأیید هویت گوگل با فایربیس: ${e.localizedMessage}"
                )
            }
        }
    }

    fun signOut() {
        auth?.signOut()
    }
}
