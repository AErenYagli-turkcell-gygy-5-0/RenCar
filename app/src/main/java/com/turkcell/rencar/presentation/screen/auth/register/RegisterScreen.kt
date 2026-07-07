package com.turkcell.rencar.presentation.screen.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.screen.auth.login.TurkishPhoneNumberVisualTransformation
import com.turkcell.rencar.presentation.theme.RenCarPrimaryLight

@Composable
fun RegisterRoute(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateBack -> onNavigateBack()
                is RegisterEffect.NavigateToOtp -> onNavigateToOtp(effect.phoneNumber)
            }
        }
    }

    RegisterScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    modifier: Modifier = Modifier,
    onIntent: (RegisterIntent) -> Unit,
) {
    val backgroundColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = !state.isLoading) {
                            onIntent(RegisterIntent.BackClicked)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 28.dp)
                )

                Text(
                    text = stringResource(R.string.register_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                RegisterTextField(
                    label = stringResource(R.string.register_full_name_label),
                    value = state.fullName,
                    enabled = !state.isLoading,
                    keyboardType = KeyboardType.Text,
                    onValueChange = { onIntent(RegisterIntent.FullNameChanged(it)) },
                    modifier = Modifier.padding(top = 28.dp)
                )

                RegisterTextField(
                    label = stringResource(R.string.register_email_label),
                    value = state.email,
                    enabled = !state.isLoading,
                    keyboardType = KeyboardType.Email,
                    onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
                    modifier = Modifier.padding(top = 18.dp)
                )

                RegisterTextField(
                    label = stringResource(R.string.register_password_label),
                    value = state.password,
                    enabled = !state.isLoading,
                    keyboardType = KeyboardType.Password,
                    onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                    isPassword = true,
                    modifier = Modifier.padding(top = 18.dp)
                )

                Text(
                    text = stringResource(R.string.register_phone_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 18.dp, bottom = 9.dp)
                )

                Row {
                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(56.dp)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "TR", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = stringResource(R.string.login_country_code),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 7.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                            .height(56.dp)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = state.phoneNumber,
                            onValueChange = { onIntent(RegisterIntent.PhoneNumberChanged(it)) },
                            enabled = !state.isLoading,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            visualTransformation = TurkishPhoneNumberVisualTransformation,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (state.phoneNumber.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.login_phone_placeholder),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                state.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .height(56.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = RenCarPrimaryLight,
                            spotColor = RenCarPrimaryLight
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            RenCarPrimaryLight.copy(
                                alpha = if (state.isLoading) 0.55f else 1f
                            )
                        )
                        .clickable(enabled = !state.isLoading) {
                            onIntent(RegisterIntent.CreateAccountClicked)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_message_bubble),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (state.isLoading) {
                                stringResource(R.string.register_submit_loading)
                            } else {
                                stringResource(R.string.register_submit_default)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.register_login_prompt_prefix))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append(stringResource(R.string.register_login_prompt_action))
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.isLoading) {
                        onIntent(RegisterIntent.LoginClicked)
                    }
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RegisterTextField(
    label: String,
    value: String,
    enabled: Boolean,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 9.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}