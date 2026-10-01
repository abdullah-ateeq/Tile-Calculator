package com.example.tilecalculator.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tilecalculator.model.MeasurementUnit
import com.example.tilecalculator.model.SurfaceType
import com.example.tilecalculator.model.TilePreset
import com.example.tilecalculator.ui.components.ResultCard
import com.example.tilecalculator.ui.viewmodel.CalculatorUiState
import com.example.tilecalculator.ui.viewmodel.TileCalculatorViewModel
import com.example.tilecalculator.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: TileCalculatorViewModel,
    uiState: CalculatorUiState,
    onNavigateToHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TILE CALCULATOR",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Estimate tiles, waste, and boxes in seconds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.semantics { contentDescription = "View calculation history" }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 768.dp

            if (isWideScreen) {
                // Two-column layout for tablets and landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CalculatorInputs(viewModel = viewModel, uiState = uiState)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.result != null) {
                            val l = uiState.length.toDoubleOrNull() ?: 0.0
                            val w = uiState.width.toDoubleOrNull() ?: 0.0
                            val tl = uiState.tileLength.toDoubleOrNull() ?: 0.0
                            val tw = uiState.tileWidth.toDoubleOrNull() ?: 0.0

                            ResultCard(
                                result = uiState.result,
                                surfaceType = uiState.surfaceType,
                                length = l,
                                width = w,
                                areaUnit = uiState.areaUnit,
                                tileLength = tl,
                                tileWidth = tw,
                                tileUnit = uiState.tileUnit,
                                wastePercent = uiState.wastePercent,
                                isSaved = uiState.isSaved,
                                onSaveClick = { viewModel.saveCurrentCalculation() }
                            )
                        } else {
                            EmptyResultPlaceholder()
                        }
                    }
                }
            } else {
                // Single column layout for phones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorInputs(viewModel = viewModel, uiState = uiState)

                    if (uiState.result != null) {
                        val l = uiState.length.toDoubleOrNull() ?: 0.0
                        val w = uiState.width.toDoubleOrNull() ?: 0.0
                        val tl = uiState.tileLength.toDoubleOrNull() ?: 0.0
                        val tw = uiState.tileWidth.toDoubleOrNull() ?: 0.0

                        ResultCard(
                            result = uiState.result,
                            surfaceType = uiState.surfaceType,
                            length = l,
                            width = w,
                            areaUnit = uiState.areaUnit,
                            tileLength = tl,
                            tileWidth = tw,
                            tileUnit = uiState.tileUnit,
                            wastePercent = uiState.wastePercent,
                            isSaved = uiState.isSaved,
                            onSaveClick = { viewModel.saveCurrentCalculation() }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorInputs(
    viewModel: TileCalculatorViewModel,
    uiState: CalculatorUiState
) {
    // Surface Mode Selector: Floor / Wall
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SurfaceType.entries.forEachIndexed { index, type ->
            SegmentedButton(
                selected = uiState.surfaceType == type,
                onClick = { viewModel.onSurfaceTypeChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = SurfaceType.entries.size)
            ) {
                Text(
                    text = type.displayName.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (uiState.surfaceType == type) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }
        }
    }

    // 1. AREA SECTION
    SectionCard(
        title = uiState.surfaceType.areaLabel.uppercase(),
        icon = Icons.Outlined.AspectRatio
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = uiState.length,
                onValueChange = { viewModel.onLengthChange(it) },
                label = { Text("Length") },
                placeholder = { Text("e.g. 5.0") },
                isError = uiState.lengthError != null,
                supportingText = uiState.lengthError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Area length" }
            )

            OutlinedTextField(
                value = uiState.width,
                onValueChange = { viewModel.onWidthChange(it) },
                label = { Text("Width") },
                placeholder = { Text("e.g. 4.0") },
                isError = uiState.widthError != null,
                supportingText = uiState.widthError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Area width" }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Unit Selector & Live Area Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UnitSelector(
                selectedUnit = uiState.areaUnit,
                onUnitSelected = { viewModel.onAreaUnitChange(it) },
                label = "Area Unit"
            )

            // Live Area Preview
            if (uiState.liveAreaSquareMeters != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AREA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = UnitConverter.formatSquareMeters(uiState.liveAreaSquareMeters),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // 2. TILE SIZE SECTION
    SectionCard(
        title = "TILE SIZE",
        icon = Icons.Outlined.GridOn
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = uiState.tileLength,
                onValueChange = { viewModel.onTileLengthChange(it) },
                label = { Text("Tile Length") },
                placeholder = { Text("e.g. 60") },
                isError = uiState.tileLengthError != null,
                supportingText = uiState.tileLengthError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Tile length" }
            )

            OutlinedTextField(
                value = uiState.tileWidth,
                onValueChange = { viewModel.onTileWidthChange(it) },
                label = { Text("Tile Width") },
                placeholder = { Text("e.g. 60") },
                isError = uiState.tileWidthError != null,
                supportingText = uiState.tileWidthError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Tile width" }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tile Unit Selector & Live Tile Area Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UnitSelector(
                selectedUnit = uiState.tileUnit,
                onUnitSelected = { viewModel.onTileUnitChange(it) },
                label = "Tile Unit"
            )

            if (uiState.liveTileAreaSquareMeters != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TILE AREA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = UnitConverter.formatSquareMeters(uiState.liveTileAreaSquareMeters, 4),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Chips
        Text(
            text = "Popular Tile Sizes",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TilePreset.standardPresets.forEachIndexed { index, preset ->
                FilterChip(
                    selected = uiState.selectedPresetIndex == index,
                    onClick = { viewModel.onSelectTilePreset(preset, index) },
                    label = { Text(preset.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
            FilterChip(
                selected = uiState.selectedPresetIndex == null,
                onClick = { viewModel.onCustomTilePresetSelected() },
                label = { Text("Custom", style = MaterialTheme.typography.labelMedium) }
            )
        }
    }

    // 3. WASTE ALLOWANCE SECTION
    SectionCard(
        title = "WASTE ALLOWANCE",
        icon = Icons.Outlined.Percent
    ) {
        Text(
            text = "Adds extra tiles for cuts, breakage, and installation waste.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickWastes = listOf(5.0, 10.0, 15.0, 20.0)
            quickWastes.forEach { pct ->
                val isSelected = !uiState.isCustomWaste && uiState.wastePercent == pct
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onSelectWasteChip(pct) },
                    label = {
                        Text(
                            "${pct.toInt()}%",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "${pct.toInt()} percent waste"
                    }
                )
            }

            FilterChip(
                selected = uiState.isCustomWaste,
                onClick = { viewModel.onSelectCustomWaste() },
                label = {
                    Text(
                        "Custom",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (uiState.isCustomWaste) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            )
        }

        AnimatedVisibility(
            visible = uiState.isCustomWaste,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                OutlinedTextField(
                    value = uiState.customWasteInput,
                    onValueChange = { viewModel.onCustomWasteChange(it) },
                    label = { Text("Custom Waste Percentage (%)") },
                    placeholder = { Text("e.g. 12") },
                    isError = uiState.wasteError != null,
                    supportingText = uiState.wasteError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 4. OPTIONAL PURCHASE DETAILS SECTION (Collapsible)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PURCHASE DETAILS (OPTIONAL)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Boxes and cost estimation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { viewModel.togglePurchaseSection() }) {
                    Icon(
                        imageVector = if (uiState.isPurchaseSectionExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (uiState.isPurchaseSectionExpanded) "Collapse purchase section" else "Expand purchase section"
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.isPurchaseSectionExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.tilesPerBox,
                        onValueChange = { viewModel.onTilesPerBoxChange(it) },
                        label = { Text("Tiles Per Box") },
                        placeholder = { Text("e.g. 8 (optional)") },
                        isError = uiState.tilesPerBoxError != null,
                        supportingText = uiState.tilesPerBoxError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.pricePerTile,
                            onValueChange = { viewModel.onPricePerTileChange(it) },
                            label = { Text("Price Per Tile") },
                            placeholder = { Text("e.g. 2.50") },
                            isError = uiState.pricePerTileError != null,
                            supportingText = uiState.pricePerTileError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = uiState.pricePerBox,
                            onValueChange = { viewModel.onPricePerBoxChange(it) },
                            label = { Text("Price Per Box") },
                            placeholder = { Text("e.g. 20.00") },
                            isError = uiState.pricePerBoxError != null,
                            supportingText = uiState.pricePerBoxError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // 5. ACTION BUTTONS: CALCULATE & RESET
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = { viewModel.resetCalculator() },
            modifier = Modifier
                .weight(0.7f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Clear,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RESET",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Button(
            onClick = { viewModel.calculateTiles() },
            modifier = Modifier
                .weight(1.3f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Calculate,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CALCULATE TILES",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitSelector(
    selectedUnit: MeasurementUnit,
    onUnitSelected: (MeasurementUnit) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "${selectedUnit.symbol} (${selectedUnit.displayName})",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .width(185.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MeasurementUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text("${unit.symbol} — ${unit.displayName}") },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyResultPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Results Preview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter dimensions and tap Calculate Tiles to generate complete material requirements.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
