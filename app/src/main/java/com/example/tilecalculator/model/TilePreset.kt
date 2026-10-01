package com.example.tilecalculator.model

/**
 * Common standard tile size presets.
 */
data class TilePreset(
    val label: String,
    val length: Double,
    val width: Double,
    val unit: MeasurementUnit = MeasurementUnit.CENTIMETER
) {
    companion object {
        val standardPresets = listOf(
            TilePreset("30 × 30 cm", 30.0, 30.0, MeasurementUnit.CENTIMETER),
            TilePreset("40 × 40 cm", 40.0, 40.0, MeasurementUnit.CENTIMETER),
            TilePreset("45 × 45 cm", 45.0, 45.0, MeasurementUnit.CENTIMETER),
            TilePreset("50 × 50 cm", 50.0, 50.0, MeasurementUnit.CENTIMETER),
            TilePreset("60 × 60 cm", 60.0, 60.0, MeasurementUnit.CENTIMETER),
            TilePreset("30 × 60 cm", 30.0, 60.0, MeasurementUnit.CENTIMETER),
            TilePreset("60 × 120 cm", 60.0, 120.0, MeasurementUnit.CENTIMETER)
        )
    }
}
