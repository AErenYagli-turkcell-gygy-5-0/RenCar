package com.turkcell.rencar.presentation.component.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.turkcell.rencar.presentation.theme.RenCarExtendedColors
import com.turkcell.rencar.presentation.theme.extendedColors
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

// Kadıköy çevresi — HomeScreen'in varsayılan harita merkezi (konum izni yoksa/gelmeden önce kullanılır).
val DEFAULT_CENTER = LatLng(latitude = 40.9909, longitude = 29.0304)
const val DEFAULT_ZOOM = 14.0

private const val SOURCE_ME = "me"
private const val LAYER_ME = "me-layer"

/**
 * RencarMap tarafından geri verilen, harita kamerasını dışarıdan kontrol etmeye yarayan basit bir tutamaç.
 */
class RencarMapController internal constructor(
    private val mapLibreMap: MapLibreMap
) {
    fun animateTo(target: LatLng, zoom: Double = DEFAULT_ZOOM) {
        mapLibreMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(MapLibreLatLng(target.latitude, target.longitude))
                    .zoom(zoom)
                    .build()
            )
        )
    }
}

@Composable
fun RencarMap(
    modifier: Modifier = Modifier,
    initialCenter: LatLng = DEFAULT_CENTER,
    initialZoom: Double = DEFAULT_ZOOM,
    myLocation: LatLng?,
    vehicles: List<VehicleMarker> = emptyList(),
    onControllerReady: (RencarMapController) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val categoryColors = MaterialTheme.extendedColors
    val density = LocalDensity.current
    val statusBarInsetPx = WindowInsets.statusBars.getTop(density)

    val currentMyLocation = rememberUpdatedState(myLocation)
    val currentVehicles = rememberUpdatedState(vehicles)
    val currentCategoryColors = rememberUpdatedState(categoryColors)
    val currentOnControllerReady = rememberUpdatedState(onControllerReady)

    val mapView = remember { MapView(context.also { MapLibre.getInstance(it) }) }
    val meSourceState = remember { mutableStateOf<GeoJsonSource?>(null) }
    val styleState = remember { mutableStateOf<Style?>(null) }
    val symbolManagerState = remember { mutableStateOf<SymbolManager?>(null) }
    val mapLibreMapState = remember { mutableStateOf<MapLibreMap?>(null) }
    val addedIconIds = remember { mutableStateOf(mutableSetOf<String>()) }

    LaunchedEffect(mapLibreMapState.value, statusBarInsetPx) {
        val map = mapLibreMapState.value ?: return@LaunchedEffect
        val edgeMarginPx = with(density) { 16.dp.roundToPx() }
        val topExtraPx = with(density) { 16.dp.roundToPx() }
        map.uiSettings.setCompassFadeFacingNorth(false)
        map.uiSettings.setCompassMargins(
            edgeMarginPx,
            statusBarInsetPx + topExtraPx,
            edgeMarginPx,
            edgeMarginPx
        )
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            mapView.getMapAsync { mapLibreMap ->
                mapLibreMapState.value = mapLibreMap
                mapLibreMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(MapLibreLatLng(initialCenter.latitude, initialCenter.longitude))
                            .zoom(initialZoom)
                            .build()
                    )
                )

                mapLibreMap.setStyle(Style.Builder().fromJson(OSM_STYLE_JSON)) { style ->
                    val meSource = GeoJsonSource(SOURCE_ME)
                    style.addSource(meSource)
                    style.addLayer(
                        CircleLayer(LAYER_ME, SOURCE_ME).withProperties(
                            PropertyFactory.circleRadius(7f),
                            PropertyFactory.circleColor("#0B6BCB"),
                            PropertyFactory.circleStrokeWidth(2f),
                            PropertyFactory.circleStrokeColor("#FFFFFF")
                        )
                    )
                    updateMeSource(meSource, currentMyLocation.value)
                    meSourceState.value = meSource
                    styleState.value = style

                    val symbolManager = SymbolManager(mapView, mapLibreMap, style)
                    symbolManagerState.value = symbolManager
                    updateVehicleSymbols(
                        context = context,
                        style = style,
                        symbolManager = symbolManager,
                        vehicles = currentVehicles.value,
                        categoryColors = currentCategoryColors.value,
                        addedIconIds = addedIconIds.value
                    )
                }

                currentOnControllerReady.value(RencarMapController(mapLibreMap))
            }
            mapView
        },
        update = {
            meSourceState.value?.let { updateMeSource(it, myLocation) }

            val style = styleState.value
            val symbolManager = symbolManagerState.value
            if (style != null && symbolManager != null) {
                updateVehicleSymbols(
                    context = context,
                    style = style,
                    symbolManager = symbolManager,
                    vehicles = vehicles,
                    categoryColors = categoryColors,
                    addedIconIds = addedIconIds.value
                )
            }
        }
    )
}

private fun updateMeSource(source: GeoJsonSource, location: LatLng?) {
    if (location == null) {
        source.setGeoJson(FeatureCollection.fromFeatures(emptyArray()))
    } else {
        source.setGeoJson(Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude)))
    }
}

private fun updateVehicleSymbols(
    context: Context,
    style: Style,
    symbolManager: SymbolManager,
    vehicles: List<VehicleMarker>,
    categoryColors: RenCarExtendedColors,
    addedIconIds: MutableSet<String>
) {
    symbolManager.deleteAll()
    vehicles.forEach { vehicle ->
        val iconId = "vehicle_marker_${vehicle.category.name}_${vehicle.price}"
        if (addedIconIds.add(iconId)) {
            val backgroundColor = vehicle.category.color(categoryColors).toArgb()
            style.addImage(
                iconId,
                createPriceBubbleBitmap(
                    context = context,
                    priceText = "₺${vehicle.price}",
                    backgroundColorArgb = backgroundColor
                )
            )
        }
        symbolManager.create(
            SymbolOptions()
                .withLatLng(MapLibreLatLng(vehicle.latitude, vehicle.longitude))
                .withIconImage(iconId)
                .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
        )
    }
}

// Fiyat balonunu (renkli yuvarlak arka plan + konum noktası + fiyat metni) tek bir bitmap olarak
// üretir; MapLibre Annotation eklentisindeki SymbolOptions.withTextField bilinen bir görünürlük
// hatasına sahip olduğundan (bkz. maplibre/maplibre-plugins-android #60) metin bitmap'e gömülür.
private fun createPriceBubbleBitmap(
    context: Context,
    priceText: String,
    backgroundColorArgb: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val pinRadius = 5f * density
    val gapAfterPin = 6f * density
    val paddingStart = 10f * density
    val paddingEnd = 14f * density
    val paddingVertical = 8f * density

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 13f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }
    val textWidth = textPaint.measureText(priceText)
    val fontMetrics = textPaint.fontMetrics
    val textHeight = fontMetrics.descent - fontMetrics.ascent

    val contentWidth = pinRadius * 2 + gapAfterPin + textWidth
    val width = (paddingStart + contentWidth + paddingEnd).toInt().coerceAtLeast(1)
    val height = (textHeight + paddingVertical * 2).toInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColorArgb
    }
    canvas.drawRoundRect(
        RectF(0f, 0f, width.toFloat(), height.toFloat()),
        height / 2f,
        height / 2f,
        backgroundPaint
    )

    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    val pinCenterX = paddingStart + pinRadius
    canvas.drawCircle(pinCenterX, height / 2f, pinRadius, pinPaint)

    val textX = paddingStart + pinRadius * 2 + gapAfterPin
    val textY = height / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(priceText, textX, textY, textPaint)

    return bitmap
}
