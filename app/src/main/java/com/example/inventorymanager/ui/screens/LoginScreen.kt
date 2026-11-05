package com.example.inventorymanager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventorymanager.R

@Composable
fun LoginScreen(
    onEmailLogin: (email: String, password: String, isSignUp: Boolean) -> Unit,
    onGoogleLogin: () -> Unit,
    isProcessing: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Invento",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                singleLine = true,
                enabled = !isProcessing
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onEmailLogin(email.trim(), password.trim(), isSignUpMode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isProcessing && email.isNotBlank() && password.isNotBlank(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text(if (isSignUpMode) "Create Account" else "Sign In")
            }

            TextButton(
                onClick = { isSignUpMode = !isSignUpMode },
                enabled = !isProcessing,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = " OR ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            OutlinedButton(
                onClick = onGoogleLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isProcessing,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.id_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Sign in with Google")
            }
        }

        if (isProcessing) {
            LoginLoadingOverlay()
        }
    }
}

@Composable
fun LoginLoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation),
                        color = Color(0xFFD32F2F),
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .scale(scale),
                        tint = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Signing in...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Please wait a moment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(onEmailLogin = { _, _, _ -> }, onGoogleLogin = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    MaterialTheme {
        LoginScreen(onEmailLogin = { _, _, _ -> }, onGoogleLogin = {}, isProcessing = true)
    }
}
