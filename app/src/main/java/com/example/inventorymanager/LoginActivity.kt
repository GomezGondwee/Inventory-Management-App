package com.example.inventorymanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.inventorymanager.ui.screens.LoginScreen
import com.example.inventorymanager.ui.theme.InventoryManagerTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var setProcessing: ((Boolean) -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                setProcessing?.invoke(false)
                Log.w("Login", "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            setProcessing?.invoke(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var isCheckingAuth by mutableStateOf(true)

        splashScreen.setKeepOnScreenCondition { isCheckingAuth }

        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startMainActivity()
            return
        }

        isCheckingAuth = false

        setContent {
            InventoryManagerTheme {
                var isProcessing by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                setProcessing = { isProcessing = it }

                LoginScreen(
                    onEmailLogin = { rawEmail, rawPassword, isSignUp ->
                        val email = rawEmail.trim()
                        val password = rawPassword.trim()
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email and password cannot be empty"
                            return@LoginScreen
                        }

                        isProcessing = true
                        errorMessage = null

                        if (isSignUp) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isProcessing = false
                                    if (task.isSuccessful) {
                                        startMainActivity()
                                    } else {
                                        errorMessage = task.exception?.localizedMessage ?: "Account creation failed"
                                    }
                                }
                        } else {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        isProcessing = false
                                        startMainActivity()
                                    } else {
                                        val exception = task.exception
                                        if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                                            auth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener { createTask ->
                                                    isProcessing = false
                                                    if (createTask.isSuccessful) {
                                                        startMainActivity()
                                                    } else {
                                                        errorMessage = createTask.exception?.localizedMessage ?: "Account creation failed"
                                                    }
                                                }
                                        } else {
                                            isProcessing = false
                                            errorMessage = exception?.localizedMessage ?: "Login failed"
                                        }
                                    }
                                }
                        }
                    },
                    onGoogleLogin = {
                        isProcessing = true
                        errorMessage = null
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(this, gso)
                        
                        // Force show the account picker by signing out of the Google client first
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    isProcessing = isProcessing,
                    errorMessage = errorMessage
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                setProcessing?.invoke(false)
                if (task.isSuccessful) {
                    startMainActivity()
                } else {
                    Log.w("Login", "Firebase Auth with Google failed", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
