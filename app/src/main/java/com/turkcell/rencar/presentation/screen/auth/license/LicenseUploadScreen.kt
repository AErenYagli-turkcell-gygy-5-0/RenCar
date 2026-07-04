package com.turkcell.rencar.presentation.screen.auth.license

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.theme.RenCarTheme
import com.turkcell.rencar.presentation.theme.extendedColors
import java.io.ByteArrayOutputStream

@Composable
fun LicenseUploadRoute(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicenseUploadViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val frontImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onIntent(LicenseUploadIntent.FrontImageSelected(it)) } }
    val backImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onIntent(LicenseUploadIntent.BackImageSelected(it)) } }
    val selfieCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val preview = ByteArrayOutputStream().use { output ->
                it.compress(Bitmap.CompressFormat.JPEG, SELFIE_JPEG_QUALITY, output)
                output.toByteArray()
            }
            viewModel.onIntent(LicenseUploadIntent.SelfieCaptured(preview))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LicenseUploadEffect.NavigateBack -> onNavigateBack()
                LicenseUploadEffect.OpenSelfieCamera -> selfieCamera.launch(null)
                LicenseUploadEffect.NavigateHome -> onNavigateHome()
                LicenseUploadEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    LicenseUploadScreen(
        state = state,
        onIntent = { intent ->
            when (intent) {
                LicenseUploadIntent.FrontUploadClicked ->
                    frontImagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )

                LicenseUploadIntent.BackUploadClicked ->
                    backImagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )

                else -> viewModel.onIntent(intent)
            }
        },
        modifier = modifier
    )
}

@Composable
fun LicenseUploadScreen(
    state: LicenseUploadState,
    onIntent: (LicenseUploadIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Header(onBack = { onIntent(LicenseUploadIntent.BackClicked) })
        VerificationSteps(currentStep = state.currentStep)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            when (state.currentStep) {
                LicenseVerificationStep.LOADING -> LoadingContent(
                    errorMessage = state.errorMessage,
                    onRetry = { onIntent(LicenseUploadIntent.RetryStatusClicked) }
                )

                LicenseVerificationStep.LICENSE -> LicenseContent(state, onIntent)
                LicenseVerificationStep.SELFIE -> SelfieContent(state, onIntent)
                LicenseVerificationStep.APPROVAL -> ApprovalContent(state, onIntent)
            }
        }

        if (
            state.currentStep == LicenseVerificationStep.LICENSE ||
            state.currentStep == LicenseVerificationStep.SELFIE
        ) {
            PrimaryButton(
                text = if (state.isUploading) {
                    stringResource(R.string.license_continue_loading)
                } else {
                    stringResource(R.string.license_continue_button)
                },
                isLoading = state.isUploading,
                enabled = !state.isUploading,
                onClick = { onIntent(LicenseUploadIntent.ContinueClicked) }
            )
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(
                text = stringResource(R.string.license_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.license_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VerificationSteps(currentStep: LicenseVerificationStep) {
    val activeStep = when (currentStep) {
        LicenseVerificationStep.LOADING,
        LicenseVerificationStep.LICENSE -> 1
        LicenseVerificationStep.SELFIE -> 2
        LicenseVerificationStep.APPROVAL -> 3
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LicenseStep("1", stringResource(R.string.license_step_license), activeStep == 1)
        StepDivider()
        LicenseStep("2", stringResource(R.string.license_step_selfie), activeStep == 2)
        StepDivider()
        LicenseStep("3", stringResource(R.string.license_step_approval), activeStep == 3)
    }
}

@Composable
private fun RowScope.StepDivider() {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun LicenseContent(
    state: LicenseUploadState,
    onIntent: (LicenseUploadIntent) -> Unit
) {
    Column {
        state.rejectReason?.let {
            MessageCard(text = stringResource(R.string.license_rejected_reason, it))
            Spacer(Modifier.height(12.dp))
        }
        LicenseUploadBox(
            label = stringResource(R.string.license_front_label),
            isUploaded = state.frontImageUri != null,
            placeholderText = stringResource(R.string.license_front_placeholder),
            onClick = { onIntent(LicenseUploadIntent.FrontUploadClicked) }
        )
        Spacer(Modifier.height(16.dp))
        LicenseUploadBox(
            label = stringResource(R.string.license_back_label),
            isUploaded = state.backImageUri != null,
            placeholderText = stringResource(R.string.license_back_placeholder),
            onClick = { onIntent(LicenseUploadIntent.BackUploadClicked) }
        )
        state.errorMessage?.let {
            ErrorText(text = it)
        }
    }
}

@Composable
private fun SelfieContent(
    state: LicenseUploadState,
    onIntent: (LicenseUploadIntent) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.selfie_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.selfie_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onIntent(LicenseUploadIntent.SelfieCaptureClicked) },
            contentAlignment = Alignment.Center
        ) {
            val preview = remember(state.selfiePreview) {
                state.selfiePreview?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
                }
            }
            if (preview != null) {
                Image(
                    bitmap = preview,
                    contentDescription = stringResource(R.string.selfie_preview),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Text(
            text = if (state.selfiePreview == null) {
                stringResource(R.string.selfie_capture)
            } else {
                stringResource(R.string.selfie_retake)
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable { onIntent(LicenseUploadIntent.SelfieCaptureClicked) }
        )
        state.errorMessage?.let {
            ErrorText(text = it)
        }
    }
}

@Composable
private fun ApprovalContent(
    state: LicenseUploadState,
    onIntent: (LicenseUploadIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.extendedColors.successAccent,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = stringResource(R.string.approval_waiting_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = stringResource(R.string.approval_waiting_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        state.errorMessage?.let {
            ErrorText(text = it)
        }
        PrimaryButton(
            text = stringResource(R.string.approval_check_status),
            isLoading = state.isCheckingStatus,
            enabled = !state.isCheckingStatus,
            onClick = { onIntent(LicenseUploadIntent.CheckStatusClicked) },
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
private fun LoadingContent(errorMessage: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage == null) {
            CircularProgressIndicator()
        } else {
            ErrorText(text = errorMessage)
            PrimaryButton(
                text = stringResource(R.string.common_retry),
                isLoading = false,
                enabled = true,
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
private fun MessageCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.55f)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun LicenseStep(number: String, label: String, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.extendedColors.disabledContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.extendedColors.disabledContent
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.extendedColors.disabledContent,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun LicenseUploadBox(
    label: String,
    isUploaded: Boolean,
    placeholderText: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    if (isUploaded) Modifier
                    else Modifier.dashedBorder(MaterialTheme.colorScheme.outline)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isUploaded) {
                Text(
                    text = stringResource(R.string.license_uploaded),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.extendedColors.successAccent
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 18.dp,
    strokeWidth: Dp = 2.dp
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Preview(showBackground = true)
@Composable
private fun LicenseUploadPreview() {
    RenCarTheme {
        LicenseUploadScreen(
            state = LicenseUploadState(currentStep = LicenseVerificationStep.LICENSE),
            onIntent = {}
        )
    }
}

private const val SELFIE_JPEG_QUALITY = 80
