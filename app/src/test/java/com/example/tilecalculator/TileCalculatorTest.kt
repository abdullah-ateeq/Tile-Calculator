package com.example.tilecalculator

import com.example.tilecalculator.model.MeasurementUnit
import com.example.tilecalculator.model.SurfaceType
import com.example.tilecalculator.model.TileCalculationInput
import com.example.tilecalculator.util.TileCalculationEngine
import com.example.tilecalculator.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive unit tests for Tile Calculator core logic, unit conversions,
 * ceiling rounding, waste allowance, box calculations, and input validation.
 */
class TileCalculatorTest {

    private val delta = 0.0001

    // 1. Meters calculation
    @Test
    fun testMetersCalculation() {
        val meters = UnitConverter.convertToMeters(5.0, MeasurementUnit.METER)
        assertEquals(5.0, meters, delta)
    }

    // 2. Centimeters conversion
    @Test
    fun testCentimetersConversion() {
        val meters = UnitConverter.convertToMeters(60.0, MeasurementUnit.CENTIMETER)
        assertEquals(0.60, meters, delta)

        val backToCm = UnitConverter.convertFromMeters(meters, MeasurementUnit.CENTIMETER)
        assertEquals(60.0, backToCm, delta)
    }

    // 3. Millimeters conversion
    @Test
    fun testMillimetersConversion() {
        val meters = UnitConverter.convertToMeters(600.0, MeasurementUnit.MILLIMETER)
        assertEquals(0.60, meters, delta)

        val backToMm = UnitConverter.convertFromMeters(meters, MeasurementUnit.MILLIMETER)
        assertEquals(600.0, backToMm, delta)
    }

    // 4. Inches conversion
    @Test
    fun testInchesConversion() {
        // 12 inches = 0.3048 meters
        val meters = UnitConverter.convertToMeters(12.0, MeasurementUnit.INCH)
        assertEquals(0.3048, meters, delta)

        val backToInches = UnitConverter.convertFromMeters(meters, MeasurementUnit.INCH)
        assertEquals(12.0, backToInches, delta)
    }

    // 5. Feet conversion
    @Test
    fun testFeetConversion() {
        // 10 feet = 3.048 meters
        val meters = UnitConverter.convertToMeters(10.0, MeasurementUnit.FOOT)
        assertEquals(3.048, meters, delta)

        val backToFeet = UnitConverter.convertFromMeters(meters, MeasurementUnit.FOOT)
        assertEquals(10.0, backToFeet, delta)
    }

    // 6. Area calculation
    @Test
    fun testAreaCalculation() {
        val area = UnitConverter.calculateAreaSquareMeters(5.0, 4.0, MeasurementUnit.METER)
        assertEquals(20.0, area, delta)
    }

    // 7. Tile area calculation
    @Test
    fun testTileAreaCalculation() {
        val tileArea = UnitConverter.calculateAreaSquareMeters(60.0, 60.0, MeasurementUnit.CENTIMETER)
        assertEquals(0.36, tileArea, delta)
    }

    // 8. Base tile ceiling calculation
    @Test
    fun testBaseTileCeilingCalculation() {
        // 20 m² / 0.36 m² = 55.555... -> Ceil must be 56
        val baseTiles = TileCalculationEngine.safeCeil(20.0, 0.36)
        assertEquals(56, baseTiles)

        // Exact match: 1.0 m² / 0.25 m² = 4.0 -> Ceil must be 4
        val exactTiles = TileCalculationEngine.safeCeil(1.0, 0.25)
        assertEquals(4, exactTiles)
    }

    // Specification Example Test:
    // Room: 5m x 4m -> 20 m²
    // Tile: 60cm x 60cm -> 0.36 m²
    // Base requirement: 56 tiles
    // Waste: 10% -> 6 tiles
    // Total: 62 tiles
    @Test
    fun testStandardSpecificationExample() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = 10.0
        )
        val result = TileCalculationEngine.calculateTiles(input)
        assertNotNull(result)
        assertEquals(20.0, result!!.areaSquareMeters, delta)
        assertEquals(0.36, result.tileAreaSquareMeters, delta)
        assertEquals(56, result.baseTiles)
        assertEquals(6, result.wasteTiles)
        assertEquals(62, result.totalTiles)
    }

    // 9. 5% waste
    @Test
    fun testFivePercentWaste() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = 5.0
        )
        val result = TileCalculationEngine.calculateTiles(input)
        assertNotNull(result)
        // 56 * 0.05 = 2.8 -> Ceil = 3
        assertEquals(56, result!!.baseTiles)
        assertEquals(3, result.wasteTiles)
        assertEquals(59, result.totalTiles)
    }

    // 10. 10% waste
    @Test
    fun testTenPercentWaste() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 10.0,
            width = 10.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 1.0,
            tileWidth = 1.0,
            tileUnit = MeasurementUnit.METER,
            wastePercent = 10.0
        )
        val result = TileCalculationEngine.calculateTiles(input)
        assertNotNull(result)
        // 100 tiles * 10% = 10 waste tiles
        assertEquals(100, result!!.baseTiles)
        assertEquals(10, result.wasteTiles)
        assertEquals(110, result.totalTiles)
    }

    // 11. 15% waste
    @Test
    fun testFifteenPercentWaste() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.WALL,
            length = 5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = 15.0
        )
        val result = TileCalculationEngine.calculateTiles(input)
        assertNotNull(result)
        // 56 * 0.15 = 8.4 -> Ceil = 9
        assertEquals(56, result!!.baseTiles)
        assertEquals(9, result.wasteTiles)
        assertEquals(65, result.totalTiles)
    }

    // 12. Box calculation
    @Test
    fun testBoxCalculation() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = 10.0,
            tilesPerBox = 6 // 62 total tiles / 6 = 10.33 -> 11 boxes
        )
        val result = TileCalculationEngine.calculateTiles(input)
        assertNotNull(result)
        assertEquals(62, result!!.totalTiles)
        assertEquals(11, result.requiredBoxes)
        assertEquals(66, result.purchasedTiles)
        assertEquals(4, result.extraTiles)
    }

    // 13. Extra tile calculation
    @Test
    fun testExtraTileCalculation() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 10.0,
            width = 1.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 1.0,
            tileWidth = 1.0,
            tileUnit = MeasurementUnit.METER,
            wastePercent = 0.0,
            tilesPerBox = 4 // 10 total / 4 = 2.5 -> 3 boxes = 12 purchased -> 2 extra
        )
        val result = TileCalculationEngine.calculateTiles(input)
        assertNotNull(result)
        assertEquals(10, result!!.totalTiles)
        assertEquals(3, result.requiredBoxes)
        assertEquals(12, result.purchasedTiles)
        assertEquals(2, result.extraTiles)
    }

    // 14. Invalid zero dimensions
    @Test
    fun testInvalidZeroDimensions() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 0.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = 10.0
        )
        val validation = TileCalculationEngine.validateInput(input)
        assertFalse(validation.isValid)
        assertTrue(validation.errors.containsKey("length"))
        assertNull(TileCalculationEngine.calculateTiles(input))
    }

    // 15. Negative dimensions
    @Test
    fun testNegativeDimensions() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = -5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = -60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = 10.0
        )
        val validation = TileCalculationEngine.validateInput(input)
        assertFalse(validation.isValid)
        assertTrue(validation.errors.containsKey("length"))
        assertTrue(validation.errors.containsKey("tileWidth"))
        assertNull(TileCalculationEngine.calculateTiles(input))
    }

    // 16. Waste validation
    @Test
    fun testWasteValidation() {
        val negativeWaste = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = -5.0
        )
        val excessiveWaste = negativeWaste.copy(wastePercent = 120.0)

        val valNeg = TileCalculationEngine.validateInput(negativeWaste)
        assertFalse(valNeg.isValid)
        assertTrue(valNeg.errors.containsKey("waste"))

        val valExc = TileCalculationEngine.validateInput(excessiveWaste)
        assertFalse(valExc.isValid)
        assertTrue(valExc.errors.containsKey("waste"))
    }

    // 17. Unit conversion accuracy
    @Test
    fun testUnitConversionAccuracy() {
        // 1000 mm = 1 m
        assertEquals(1.0, UnitConverter.convertToMeters(1000.0, MeasurementUnit.MILLIMETER), delta)
        // 100 cm = 1 m
        assertEquals(1.0, UnitConverter.convertToMeters(100.0, MeasurementUnit.CENTIMETER), delta)
        // 39.3700787 inches ~ 1 meter
        val inchesInMeter = UnitConverter.convertFromMeters(1.0, MeasurementUnit.INCH)
        assertEquals(39.3701, inchesInMeter, 0.01)
        // 1 foot = 0.3048 m
        assertEquals(0.3048, UnitConverter.convertToMeters(1.0, MeasurementUnit.FOOT), delta)
    }

    // 18. Cost calculation
    @Test
    fun testCostCalculation() {
        val input = TileCalculationInput(
            surfaceType = SurfaceType.FLOOR,
            length = 5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            wastePercent = 10.0,
            tilesPerBox = 6, // 11 boxes
            pricePerTile = 2.50, // 62 tiles * 2.50 = 155.00
            pricePerBox = 18.00 // 11 boxes * 18.00 = 198.00
        )
        val result = TileCalculationEngine.calculateTiles(input)
        assertNotNull(result)
        assertEquals(155.00, result!!.estimatedTileCost!!, delta)
        assertEquals(198.00, result.estimatedBoxCost!!, delta)
    }

    // 19. History serialization roundtrip
    @Test
    fun testHistorySerialization() {
        val item = com.example.tilecalculator.model.CalculationHistoryItem(
            surfaceType = SurfaceType.FLOOR,
            length = 5.0,
            width = 4.0,
            areaUnit = MeasurementUnit.METER,
            areaSquareMeters = 20.0,
            tileLength = 60.0,
            tileWidth = 60.0,
            tileUnit = MeasurementUnit.CENTIMETER,
            tileAreaSquareMeters = 0.36,
            wastePercent = 10.0,
            baseTiles = 56,
            wasteTiles = 6,
            totalTiles = 62,
            tilesPerBox = 6,
            requiredBoxes = 11,
            purchasedTiles = 66,
            extraTiles = 4,
            pricePerTile = 2.5,
            pricePerBox = 18.0,
            estimatedCost = 198.0
        )
        val json = com.example.tilecalculator.data.CalculationHistoryJson.itemToJson(item)
        val deserialized = com.example.tilecalculator.data.CalculationHistoryJson.itemFromJson(json)

        assertNotNull(deserialized)
        assertEquals(item.id, deserialized!!.id)
        assertEquals(item.totalTiles, deserialized.totalTiles)
        assertEquals(item.requiredBoxes, deserialized.requiredBoxes)
        assertEquals(item.estimatedCost!!, deserialized.estimatedCost!!, 0.001)

        val list = listOf(item, item.copy(id = "second", totalTiles = 100))
        val listJson = com.example.tilecalculator.data.CalculationHistoryJson.listToJson(list)
        val deserializedList = com.example.tilecalculator.data.CalculationHistoryJson.listFromJson(listJson)
        assertEquals(2, deserializedList.size)
        assertEquals("second", deserializedList[1].id)
        assertEquals(100, deserializedList[1].totalTiles)
    }
}

