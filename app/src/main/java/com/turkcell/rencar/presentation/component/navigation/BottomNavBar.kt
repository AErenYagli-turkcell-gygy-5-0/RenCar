package com.turkcell.rencar.presentation.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkcell.rencar.presentation.theme.RenCarTheme

enum class BottomNavItem(
    val label: String
) {
    Map(label = "Harita"),
    History(label = "Ge\u00e7mi\u015f"),
    Wallet(label = "C\u00fczdan"),
    Profile(label = "Profil")
}

@Composable
fun BottomNavBar(
    selectedItem: BottomNavItem,
    modifier: Modifier = Modifier,
    onItemSelected: (BottomNavItem) -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surface
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(
                RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
                )
            )
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            BottomNavItem.entries.forEach { item ->
                val selected = item == selectedItem
                BottomNavBarItem(
                    item = item,
                    selected = selected,
                    color = if (selected) selectedColor else unselectedColor,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .width(126.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
        )
    }
}

@Composable
private fun BottomNavBarItem(
    color: Color,
    selected: Boolean,
    item: BottomNavItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.selected = selected
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RenCarBottomNavIcon(
            item = item,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = item.label,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Preview(name = "Bottom Nav - Light", showBackground = true, widthDp = 360)
@Composable
private fun BottomNavBarLightPreview() {
    RenCarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomNavBar(
                selectedItem = BottomNavItem.Map,
                onItemSelected = {}
            )
        }
    }
}

@Preview(name = "Bottom Nav - Dark", showBackground = true, widthDp = 360)
@Composable
private fun BottomNavBarDarkPreview() {
    RenCarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BottomNavBar(
                selectedItem = BottomNavItem.Map,
                onItemSelected = {}
            )
        }
    }
}
