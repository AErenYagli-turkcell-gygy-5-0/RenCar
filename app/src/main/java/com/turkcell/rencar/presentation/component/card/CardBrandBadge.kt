package com.turkcell.rencar.presentation.component.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turkcell.rencar.domain.cards.CardBrand

@Composable
fun CardBrandBadge(brand: CardBrand, modifier: Modifier = Modifier) {
    val (background, label) = when (brand) {
        CardBrand.VISA -> RenCarVisaBlue to "VISA"
        CardBrand.MASTERCARD -> RenCarMastercardOrange to "MC"
    }
    Surface(
        modifier = modifier.size(width = 42.dp, height = 28.dp),
        color = background,
        shape = RoundedCornerShape(7.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }
    }
}

private val RenCarVisaBlue = Color(0xFF1A56DB)
private val RenCarMastercardOrange = Color(0xFFEB5424)
