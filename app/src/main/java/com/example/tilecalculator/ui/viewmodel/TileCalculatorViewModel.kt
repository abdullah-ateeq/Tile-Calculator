package com.example.tilecalculator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tilecalculator.data.TileDataStoreRepository
import com.example.tilecalculator.model.AppSettings
import com.example.tilecalculator.model.AppTheme
import com.example.tilecalculator.model.CalculationHistoryItem
import com.example.tilecalculator.model.MeasurementUnit
import com.example.tilecalculator.model.SurfaceType
import com.example.tilecalculator.model.TileCalculationInput
import com.example.tilecalculator.model.TileCalculationResult
import com.example.tilecalculator.model.TilePreset
import com.example.tilecalculator.util.FeedbackManager
import com.example.tilecalculator.util.TileCalculationEngine
import com.example.tilecalculator.util.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val surfaceType: SurfaceType = SurfaceType.FLOOR,
    val length: String = "",
    val width: String = "",
    val areaUnit: MeasurementUnit = MeasurementUnit.METER,
    val tileLength: String = "",
    val tileWidth: String = "",
    val tileUnit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val selectedPresetIndex: Int? = null,
    val wastePercent: Double = 10.0,
    val isCustomWaste: Boolean = false,
    val customWasteInput: String = "",
    val tilesPerBox: String = "",
    val pricePerTile: String = "",
    val pricePerBox: String = "",
    val isPurchaseSectionExpanded: Boolean = false,
    val lengthError: String? = null,
    val widthError: String? = null,
    val tileLengthError: String? = null,
    val tileWidthError: String? = null,
    val wasteError: String? = null,
    val tilesPerBoxError: String? = null,
    val pricePerTileError: String? = null,
    val pricePerBoxError: String? = null,
    val liveAreaSquareMeters: Double? = null,
    val liveTileAreaSquareMeters: Double? = null,
    val result: TileCalculationResult? = null,
    val isSaved: Boolean = false,
    val selectedHistoryItemForDetail: CalculationHistoryItem? = null
)

class TileCalculatorViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: TileDataStoreRepository = TileDataStoreRepository(application),
    private val feedbackManager: FeedbackManager = FeedbackManager(application)
) : AndroidViewModel(application) {

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val history: StateFlow<List<CalculationHistoryItem>> = repository.historyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        // Observe settings changes to set defaults on initial launch
        viewModelScope.launch {
            settings.collect { currentSettings ->
                _uiState.update { current ->
                    if (current.length.isEmpty() && current.width.isEmpty() && current.result == null) {
                        current.copy(
                            areaUnit = currentSettings.defaultAreaUnit,
                            tileUnit = currentSettings.defaultTileUnit,
                            wastePercent = currentSettings.defaultWastePercent
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun onSurfaceTypeChange(type: SurfaceType) {
        _uiState.update { it.copy(surfaceType = type) }
    }

    fun onLengthChange(value: String) {
        _uiState.update { current ->
            val error = validateDimension(value, "Length")
            val newState = current.copy(length = value, lengthError = error)
            recomputeLiveArea(newState)
        }
    }

    fun onWidthChange(value: String) {
        _uiState.update { current ->
            val error = validateDimension(value, "Width")
            val newState = current.copy(width = value, widthError = error)
            recomputeLiveArea(newState)
        }
    }

    fun onAreaUnitChange(unit: MeasurementUnit) {
        _uiState.update { current ->
            val newState = current.copy(areaUnit = unit)
            recomputeLiveArea(newState)
        }
    }

    fun onTileLengthChange(value: String) {
        _uiState.update { current ->
            val error = validateDimension(value, "Tile length")
            val newState = current.copy(
                tileLength = value,
                tileLengthError = error,
                selectedPresetIndex = null
            )
            recomputeLiveTileArea(newState)
        }
    }

    fun onTileWidthChange(value: String) {
        _uiState.update { current ->
            val error = validateDimension(value, "Tile width")
            val newState = current.copy(
                tileWidth = value,
                tileWidthError = error,
                selectedPresetIndex = null
            )
            recomputeLiveTileArea(newState)
        }
    }

    fun onTileUnitChange(unit: MeasurementUnit) {
        _uiState.update { current ->
            val newState = current.copy(tileUnit = unit, selectedPresetIndex = null)
            recomputeLiveTileArea(newState)
        }
    }

    fun onSelectTilePreset(preset: TilePreset, index: Int) {
        _uiState.update { current ->
            val lengthStr = UnitConverter.formatDecimal(preset.length)
            val widthStr = UnitConverter.formatDecimal(preset.width)
            val newState = current.copy(
                tileLength = lengthStr,
                tileWidth = widthStr,
                tileUnit = preset.unit,
                selectedPresetIndex = index,
                tileLengthError = null,
                tileWidthError = null
            )
            recomputeLiveTileArea(newState)
        }
    }

    fun onCustomTilePresetSelected() {
        _uiState.update { it.copy(selectedPresetIndex = null) }
    }

    fun onSelectWasteChip(percent: Double) {
        _uiState.update {
            it.copy(
                wastePercent = percent,
                isCustomWaste = false,
                customWasteInput = "",
                wasteError = null
            )
        }
    }

    fun onSelectCustomWaste() {
        _uiState.update {
            it.copy(
                isCustomWaste = true,
                customWasteInput = if (it.isCustomWaste) it.customWasteInput else UnitConverter.formatDecimal(it.wastePercent)
            )
        }
    }

    fun onCustomWasteChange(value: String) {
        _uiState.update { current ->
            val parsed = value.toDoubleOrNull()
            val error = when {
                value.isBlank() -> "Enter waste percentage."
                parsed == null -> "Enter a valid number."
                parsed < 0.0 || parsed > 100.0 -> "Waste must be between 0% and 100%."
                else -> null
            }
            val wastePercent = if (error == null && parsed != null) parsed else current.wastePercent
            current.copy(
                customWasteInput = value,
                wastePercent = wastePercent,
                wasteError = error
            )
        }
    }

    fun onTilesPerBoxChange(value: String) {
        _uiState.update { current ->
            val error = if (value.isNotBlank()) {
                val parsed = value.toIntOrNull()
                if (parsed == null || parsed <= 0) "Must be a positive whole number." else null
            } else {
                null
            }
            current.copy(tilesPerBox = value, tilesPerBoxError = error)
        }
    }

    fun onPricePerTileChange(value: String) {
        _uiState.update { current ->
            val error = if (value.isNotBlank()) {
                val parsed = value.toDoubleOrNull()
                if (parsed == null || parsed < 0) "Enter a valid positive price." else null
            } else {
                null
            }
            current.copy(pricePerTile = value, pricePerTileError = error)
        }
    }

    fun onPricePerBoxChange(value: String) {
        _uiState.update { current ->
            val error = if (value.isNotBlank()) {
                val parsed = value.toDoubleOrNull()
                if (parsed == null || parsed < 0) "Enter a valid positive price." else null
            } else {
                null
            }
            current.copy(pricePerBox = value, pricePerBoxError = error)
        }
    }

    fun togglePurchaseSection() {
        _uiState.update { it.copy(isPurchaseSectionExpanded = !it.isPurchaseSectionExpanded) }
    }

    fun calculateTiles(): Boolean {
        val current = _uiState.value
        val lengthVal = current.length.toDoubleOrNull()
        val widthVal = current.width.toDoubleOrNull()
        val tileLengthVal = current.tileLength.toDoubleOrNull()
        val tileWidthVal = current.tileWidth.toDoubleOrNull()

        var hasError = false
        val lengthError = if (lengthVal == null || lengthVal <= 0) {
            hasError = true
            "Enter a valid length."
        } else null

        val widthError = if (widthVal == null || widthVal <= 0) {
            hasError = true
            "Enter a valid width."
        } else null

        val tileLengthError = if (tileLengthVal == null || tileLengthVal <= 0) {
            hasError = true
            "Tile length must be greater than zero."
        } else null

        val tileWidthError = if (tileWidthVal == null || tileWidthVal <= 0) {
            hasError = true
            "Tile width must be greater than zero."
        } else null

        val wasteError = if (current.wastePercent < 0.0 || current.wastePercent > 100.0) {
            hasError = true
            "Waste percentage must be between 0 and 100."
        } else null

        val tilesPerBoxVal = current.tilesPerBox.takeIf { it.isNotBlank() }?.toIntOrNull()
        val tilesPerBoxError = if (current.tilesPerBox.isNotBlank() && (tilesPerBoxVal == null || tilesPerBoxVal <= 0)) {
            hasError = true
            "Tiles per box must be greater than zero."
        } else null

        val pricePerTileVal = current.pricePerTile.takeIf { it.isNotBlank() }?.toDoubleOrNull()
        val pricePerTileError = if (current.pricePerTile.isNotBlank() && (pricePerTileVal == null || pricePerTileVal < 0)) {
            hasError = true
            "Negative tile price is invalid."
        } else null

        val pricePerBoxVal = current.pricePerBox.takeIf { it.isNotBlank() }?.toDoubleOrNull()
        val pricePerBoxError = if (current.pricePerBox.isNotBlank() && (pricePerBoxVal == null || pricePerBoxVal < 0)) {
            hasError = true
            "Negative box price is invalid."
        } else null

        if (hasError) {
            _uiState.update {
                it.copy(
                    lengthError = lengthError,
                    widthError = widthError,
                    tileLengthError = tileLengthError,
                    tileWidthError = tileWidthError,
                    wasteError = wasteError,
                    tilesPerBoxError = tilesPerBoxError,
                    pricePerTileError = pricePerTileError,
                    pricePerBoxError = pricePerBoxError
                )
            }
            feedbackManager.performWarningHaptic(settings.value.hapticFeedbackEnabled)
            return false
        }

        val input = TileCalculationInput(
            surfaceType = current.surfaceType,
            length = lengthVal!!,
            width = widthVal!!,
            areaUnit = current.areaUnit,
            tileLength = tileLengthVal!!,
            tileWidth = tileWidthVal!!,
            tileUnit = current.tileUnit,
            wastePercent = current.wastePercent,
            tilesPerBox = tilesPerBoxVal,
            pricePerTile = pricePerTileVal,
            pricePerBox = pricePerBoxVal
        )

        val result = TileCalculationEngine.calculateTiles(input)
        if (result != null) {
            _uiState.update {
                it.copy(
                    result = result,
                    isSaved = false,
                    lengthError = null,
                    widthError = null,
                    tileLengthError = null,
                    tileWidthError = null,
                    wasteError = null,
                    tilesPerBoxError = null,
                    pricePerTileError = null,
                    pricePerBoxError = null
                )
            }
            feedbackManager.performLightHaptic(settings.value.hapticFeedbackEnabled)
            feedbackManager.playCalculateSound(settings.value.soundEffectsEnabled)
            return true
        } else {
            feedbackManager.performWarningHaptic(settings.value.hapticFeedbackEnabled)
            return false
        }
    }

    fun resetCalculator() {
        val currentSettings = settings.value
        _uiState.value = CalculatorUiState(
            surfaceType = SurfaceType.FLOOR,
            areaUnit = currentSettings.defaultAreaUnit,
            tileUnit = currentSettings.defaultTileUnit,
            wastePercent = currentSettings.defaultWastePercent,
            isCustomWaste = false,
            customWasteInput = ""
        )
        feedbackManager.performLightHaptic(currentSettings.hapticFeedbackEnabled)
    }

    fun saveCurrentCalculation(): Boolean {
        val current = _uiState.value
        val result = current.result ?: return false
        if (current.isSaved) return true

        val lengthVal = current.length.toDoubleOrNull() ?: return false
        val widthVal = current.width.toDoubleOrNull() ?: return false
        val tileLengthVal = current.tileLength.toDoubleOrNull() ?: return false
        val tileWidthVal = current.tileWidth.toDoubleOrNull() ?: return false

        val item = CalculationHistoryItem(
            surfaceType = current.surfaceType,
            length = lengthVal,
            width = widthVal,
            areaUnit = current.areaUnit,
            areaSquareMeters = result.areaSquareMeters,
            tileLength = tileLengthVal,
            tileWidth = tileWidthVal,
            tileUnit = current.tileUnit,
            tileAreaSquareMeters = result.tileAreaSquareMeters,
            wastePercent = current.wastePercent,
            baseTiles = result.baseTiles,
            wasteTiles = result.wasteTiles,
            totalTiles = result.totalTiles,
            tilesPerBox = current.tilesPerBox.takeIf { it.isNotBlank() }?.toIntOrNull(),
            requiredBoxes = result.requiredBoxes,
            purchasedTiles = result.purchasedTiles,
            extraTiles = result.extraTiles,
            pricePerTile = current.pricePerTile.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
            pricePerBox = current.pricePerBox.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
            estimatedCost = result.estimatedCost
        )

        viewModelScope.launch {
            repository.saveCalculation(item)
            _uiState.update { it.copy(isSaved = true) }
            feedbackManager.performLightHaptic(settings.value.hapticFeedbackEnabled)
            feedbackManager.playSaveSound(settings.value.soundEffectsEnabled)
        }
        return true
    }

    fun loadHistoryItemIntoCalculator(item: CalculationHistoryItem) {
        val lengthStr = UnitConverter.formatDecimal(item.length)
        val widthStr = UnitConverter.formatDecimal(item.width)
        val tileLengthStr = UnitConverter.formatDecimal(item.tileLength)
        val tileWidthStr = UnitConverter.formatDecimal(item.tileWidth)

        val result = TileCalculationResult(
            areaSquareMeters = item.areaSquareMeters,
            tileAreaSquareMeters = item.tileAreaSquareMeters,
            baseTiles = item.baseTiles,
            wasteTiles = item.wasteTiles,
            totalTiles = item.totalTiles,
            requiredBoxes = item.requiredBoxes,
            purchasedTiles = item.purchasedTiles,
            extraTiles = item.extraTiles,
            estimatedCost = item.estimatedCost,
            estimatedTileCost = if (item.pricePerTile != null) item.totalTiles * item.pricePerTile else null,
            estimatedBoxCost = if (item.pricePerBox != null && item.requiredBoxes != null) item.requiredBoxes * item.pricePerBox else null
        )

        val standardWasteList = listOf(5.0, 10.0, 15.0, 20.0)
        val isCustomWaste = !standardWasteList.contains(item.wastePercent)

        // Find if preset matches
        val presetIndex = TilePreset.standardPresets.indexOfFirst {
            it.length == item.tileLength && it.width == item.tileWidth && it.unit == item.tileUnit
        }.takeIf { it >= 0 }

        _uiState.update {
            it.copy(
                surfaceType = item.surfaceType,
                length = lengthStr,
                width = widthStr,
                areaUnit = item.areaUnit,
                tileLength = tileLengthStr,
                tileWidth = tileWidthStr,
                tileUnit = item.tileUnit,
                selectedPresetIndex = presetIndex,
                wastePercent = item.wastePercent,
                isCustomWaste = isCustomWaste,
                customWasteInput = if (isCustomWaste) UnitConverter.formatDecimal(item.wastePercent) else "",
                tilesPerBox = item.tilesPerBox?.toString() ?: "",
                pricePerTile = item.pricePerTile?.let { p -> UnitConverter.formatDecimal(p) } ?: "",
                pricePerBox = item.pricePerBox?.let { p -> UnitConverter.formatDecimal(p) } ?: "",
                isPurchaseSectionExpanded = item.tilesPerBox != null || item.pricePerTile != null || item.pricePerBox != null,
                lengthError = null,
                widthError = null,
                tileLengthError = null,
                tileWidthError = null,
                wasteError = null,
                tilesPerBoxError = null,
                pricePerTileError = null,
                pricePerBoxError = null,
                liveAreaSquareMeters = item.areaSquareMeters,
                liveTileAreaSquareMeters = item.tileAreaSquareMeters,
                result = result,
                isSaved = true
            )
        }
    }

    fun setSelectedHistoryDetail(item: CalculationHistoryItem?) {
        _uiState.update { it.copy(selectedHistoryItemForDetail = item) }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteCalculation(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.updateTheme(theme)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDynamicColor(enabled)
        }
    }

    fun updateDefaultWaste(percent: Double) {
        viewModelScope.launch {
            repository.updateDefaultWaste(percent)
        }
    }

    fun updateDefaultAreaUnit(unit: MeasurementUnit) {
        viewModelScope.launch {
            repository.updateDefaultAreaUnit(unit)
        }
    }

    fun updateDefaultTileUnit(unit: MeasurementUnit) {
        viewModelScope.launch {
            repository.updateDefaultTileUnit(unit)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHapticFeedback(enabled)
        }
    }

    fun updateSoundEffects(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSoundEffects(enabled)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            repository.resetSettings()
        }
    }

    private fun validateDimension(value: String, fieldName: String): String? {
        if (value.isBlank()) return null
        val parsed = value.toDoubleOrNull()
        return when {
            parsed == null -> "$fieldName must be a valid number."
            parsed <= 0.0 -> "$fieldName must be greater than zero."
            else -> null
        }
    }

    private fun recomputeLiveArea(state: CalculatorUiState): CalculatorUiState {
        val l = state.length.toDoubleOrNull()
        val w = state.width.toDoubleOrNull()
        val area = if (l != null && w != null && l > 0 && w > 0) {
            UnitConverter.calculateAreaSquareMeters(l, w, state.areaUnit)
        } else {
            null
        }
        return state.copy(liveAreaSquareMeters = area)
    }

    private fun recomputeLiveTileArea(state: CalculatorUiState): CalculatorUiState {
        val l = state.tileLength.toDoubleOrNull()
        val w = state.tileWidth.toDoubleOrNull()
        val area = if (l != null && w != null && l > 0 && w > 0) {
            UnitConverter.calculateAreaSquareMeters(l, w, state.tileUnit)
        } else {
            null
        }
        return state.copy(liveTileAreaSquareMeters = area)
    }
}
