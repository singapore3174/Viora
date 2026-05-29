package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingOnboardingScreen(
    isDark: Boolean,
    onLoginSuccess: (email: String, provider: String) -> Unit
) {
    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF050505), Color(0xFF0B0D17), Color(0xFF131422)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0C0D14), Color(0xFF07080D), Color(0xFF020204)))
    }
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFFE2E8F0)
    val inputBg = if (isDark) Color(0x0CFFFFFF) else Color(0x08FFFFFF)
    val inputBorder = if (isDark) GlassBorderWhite else Color(0x15FFFFFF)

    var currentTab by remember { mutableStateOf("EMAIL") } // EMAIL, PHONE, GOOGLE
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var screenState by remember { mutableStateOf("WELCOME") } // WELCOME, AUTH, GOOGLE_LOADING
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Welcoming Aura Header ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(CyberpunkPink, CyberpunkPurple, CyberpunkCyan)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AURA",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Next-Generation Social Experience",
                fontSize = 14.sp,
                color = if (isDark) Color.LightGray else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (screenState == "WELCOME") {
                // Landing state with selection options
                Text(
                    text = "Discover aesthetics curated specifically for you. Real-time connections under dynamic neon filters.",
                    color = if (isDark) Color.LightGray else Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                GradientButton(
                    text = "Get Started",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "get_started_btn"
                ) {
                    screenState = "AUTH"
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Google Access mimicking AppAuth
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(
                            1.dp,
                            if (isDark) Color(0x33FFFFFF) else Color(0x22000000),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .clip(RoundedCornerShape(25.dp))
                        .clickable {
                            screenState = "GOOGLE_LOADING"
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign in with Google Account",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

            } else if (screenState == "AUTH") {
                // Main Login Panel with Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(inputBg, shape = RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentTab == "EMAIL") CyberpunkPurple else Color.Transparent)
                            .clickable {
                                currentTab = "EMAIL"
                                errorMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Email Login", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentTab == "PHONE") CyberpunkPurple else Color.Transparent)
                            .clickable {
                                currentTab = "PHONE"
                                errorMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Phone Entry", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (currentTab == "EMAIL") {
                    // Email & Password Fields
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkCyan,
                            unfocusedBorderColor = inputBorder,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Secure Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkCyan,
                            unfocusedBorderColor = inputBorder,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    GradientButton(
                        text = "Access Account",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "email_login_submit"
                    ) {
                        if (emailInput.contains("@") && passwordInput.length >= 6) {
                            onLoginSuccess(emailInput, "Email")
                        } else {
                            errorMessage = "Please enter a valid email and 6+ char password."
                        }
                    }

                } else {
                    // Phone fields
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number (+1...)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkCyan,
                            unfocusedBorderColor = inputBorder,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = { Text("Verification Code (SMS)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_code_input"),
                        placeholder = { Text("Sent automatically if typed above") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkCyan,
                            unfocusedBorderColor = inputBorder,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    GradientButton(
                        text = "Access via SMS Verification",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "phone_login_submit"
                    ) {
                        if (phoneInput.length >= 8 && verificationCode.length >= 4) {
                            onLoginSuccess(phoneInput, "Phone SMS")
                        } else {
                            errorMessage = "Provide a valid number and 4+ digit confirmation."
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Go back to start",
                    color = if (isDark) Color.Gray else Color.LightGray,
                    modifier = Modifier
                        .clickable { screenState = "WELCOME" }
                        .padding(8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

            } else if (screenState == "GOOGLE_LOADING") {
                // Google loading sign-in modal simulator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(inputBg, shape = RoundedCornerShape(24.dp))
                        .border(1.dp, inputBorder, shape = RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Google Sign-In", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = CyberpunkPink)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Securing OAuth token communication...",
                            color = if (isDark) Color.LightGray else Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                onLoginSuccess("azure_google@gmail.com", "Google Account OAuth")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberpunkCyan)
                        ) {
                            Text("Confirm Sign-In", color = Color.Black)
                        }
                    }
                }
            }
        }

        // Footer terms
        Text(
            text = "By joining Aura, you agree to dynamic community standards and real-time encryption terms.",
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}
