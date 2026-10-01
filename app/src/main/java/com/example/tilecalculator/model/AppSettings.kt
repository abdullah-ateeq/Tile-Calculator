package com.example.tilecalculator.model

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * User application settings persisted in Preferences DataStore.
 */
data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultWastePercent: Double = 10.0,
    val defaultAreaUnit: MeasurementUnit = MeasurementUnit.METER,
    val defaultTileUnit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val hapticFeedbackEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = false
)
