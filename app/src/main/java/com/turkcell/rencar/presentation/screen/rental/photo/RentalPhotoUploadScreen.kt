package com.turkcell.rencar.presentation.screen.rental.photo

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.presentation.theme.RenCarTheme
import java.io.File

@Composable
fun RentalPhotoUploadRoute(
    rentalId: String,
    vehicleId: String,
    mode: RentalPhotoUploadMode,
    onNavigateBack: () -> Unit,
    onNavigateToActiveRental: (rentalId: String, vehicleId: String) -> Unit,
    onNavigateToPayment: (rentalId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RentalPhotoUploadViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(rentalId, mode) {
        viewModel.onIntent(RentalPhotoUploadIntent.ScreenStarted(rentalId, vehicleId, mode))
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RentalPhotoUploadEffect.NavigateBack -> onNavigateBack()
                is RentalPhotoUploadEffect.NavigateToActiveRental ->
                    onNavigateToActiveRental(effect.rentalId, effect.vehicleId)

                is RentalPhotoUploadEffect.NavigateToPayment -> onNavigateToPayment(effect.rentalId)
            }
        }
    }

    RentalPhotoUploadScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun RentalPhotoUploadScreen(
    state: RentalPhotoUploadState,
    onIntent: (RentalPhotoUploadIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        PhotoUploadHeader(
            mode = state.mode,
            onBack = { onIntent(RentalPhotoUploadIntent.BackClicked) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = listOf(state.vehicleName, state.plate).filter(String::isNotBlank)
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.rental_photo_counter,
                    state.completedSides.size,
                    RentalPhotoSide.entries.size
                ),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(RentalPhotoSide.entries) { side ->
                    PhotoSlot(
                        side = side,
                        photoUri = state.photos[side],
                        isUploading = state.uploadingSide == side,
                        isDone = side in state.completedSides,
                        onSelected = { uri -> onIntent(RentalPhotoUploadIntent.PhotoSelected(side, uri)) }
                    )
                }
            }
        }

        state.errorMessage?.let { error ->
            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        PhotoUploadBottomBar(
            state = state,
            onClick = { onIntent(RentalPhotoUploadIntent.PrimaryActionClicked) }
        )
    }
}

@Composable
private fun PhotoUploadHeader(mode: RentalPhotoUploadMode, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            BackIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(
                text = stringResource(R.string.rental_photo_title),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(
                    if (mode == RentalPhotoUploadMode.START_TRIP) {
                        R.string.rental_photo_subtitle_start
                    } else {
                        R.string.rental_photo_subtitle_return
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoSlot(
    side: RentalPhotoSide,
    photoUri: Uri?,
    isUploading: Boolean,
    isDone: Boolean,
    onSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var showSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onSelected) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            onSelected(uri)
        }
        pendingCameraUri = null
    }

    val preview = remember(context, photoUri) {
        photoUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    if (showSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showSourceDialog = false },
            onCameraSelected = {
                showSourceDialog = false
                val uri = createTempImageUri(context)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            },
            onGallerySelected = {
                showSourceDialog = false
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = !isUploading) { showSourceDialog = true },
        contentAlignment = Alignment.Center
    ) {
        val sideLabel = side.toLabel()
        if (preview != null) {
            Image(
                bitmap = preview,
                contentDescription = sideLabel,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.rental_photo_capture_hint),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        Text(
            text = sideLabel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
                .padding(horizontal = 9.dp, vertical = 3.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )

        if (isDone) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.tertiary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PhotoUploadBottomBar(state: RentalPhotoUploadState, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(R.string.rental_photo_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    val actionLabel = stringResource(
                        if (state.mode == RentalPhotoUploadMode.START_TRIP) {
                            R.string.rental_photo_start_action
                        } else {
                            R.string.rental_photo_finish_action
                        }
                    )
                    val text = if (state.allPhotosReady) {
                        actionLabel
                    } else {
                        stringResource(R.string.rental_photo_action_with_remaining, actionLabel, state.remainingCount)
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = stringResource(R.string.rental_photo_source_title)) },
        text = {
            Column {
                PhotoSourceOption(
                    text = stringResource(R.string.rental_photo_source_camera),
                    onClick = onCameraSelected
                )
                PhotoSourceOption(
                    text = stringResource(R.string.rental_photo_source_gallery),
                    onClick = onGallerySelected
                )
                PhotoSourceOption(
                    text = stringResource(R.string.rental_photo_source_cancel),
                    onClick = onDismiss
                )
            }
        }
    )
}

@Composable
private fun PhotoSourceOption(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

private fun createTempImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "rental_photos").apply { mkdirs() }
    val file = File.createTempFile("rental_${System.currentTimeMillis()}_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun RentalPhotoSide.toLabel(): String = stringResource(
    when (this) {
        RentalPhotoSide.FRONT -> R.string.rental_photo_side_front
        RentalPhotoSide.BACK -> R.string.rental_photo_side_back
        RentalPhotoSide.LEFT -> R.string.rental_photo_side_left
        RentalPhotoSide.RIGHT -> R.string.rental_photo_side_right
    }
)

@Composable
private fun BackIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.18f),
            end = Offset(size.width * 0.34f, size.height * 0.5f),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.34f, size.height * 0.5f),
            end = Offset(size.width * 0.65f, size.height * 0.82f),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RentalPhotoUploadPreview() {
    RenCarTheme(darkTheme = false) {
        RentalPhotoUploadScreen(
            state = RentalPhotoUploadState(
                vehicleName = "Renault Clio",
                plate = "34 RNC 022",
                mode = RentalPhotoUploadMode.START_TRIP
            ),
            onIntent = {}
        )
    }
}
