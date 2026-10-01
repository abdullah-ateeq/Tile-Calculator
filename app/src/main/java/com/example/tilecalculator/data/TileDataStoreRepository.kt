package com.example.tilecalculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tilecalculator.model.AppSettings
import com.example.tilecalculator.model.AppTheme
import com.example.tilecalculator.model.CalculationHistoryItem
import com.example.tilecalculator.model.MeasurementUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tile_calculator_preferences")

class TileDataStoreRepository(private val context: Context) {

    companion object {
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_WASTE = doublePreferencesKey("default_waste_percent")
        private val KEY_DEFAULT_AREA_UNIT = stringPreferencesKey("default_area_unit")
        private val KEY_DEFAULT_TILE_UNIT = stringPreferencesKey("default_tile_unit")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        private val KEY_HISTORY = stringPreferencesKey("calculation_history_json")

        const val MAX_HISTORY_ITEMS = 50
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val themeString = prefs[KEY_THEME] ?: AppTheme.SYSTEM.name
            val theme = try {
                AppTheme.valueOf(themeString)
            } catch (_: Exception) {
                AppTheme.SYSTEM
            }

            val areaUnitString = prefs[KEY_DEFAULT_AREA_UNIT] ?: MeasurementUnit.METER.name
            val areaUnit = try {
                MeasurementUnit.valueOf(areaUnitString)
            } catch (_: Exception) {
                MeasurementUnit.METER
            }

            val tileUnitString = prefs[KEY_DEFAULT_TILE_UNIT] ?: MeasurementUnit.CENTIMETER.name
            val tileUnit = try {
                MeasurementUnit.valueOf(tileUnitString)
            } catch (_: Exception) {
                MeasurementUnit.CENTIMETER
            }

            AppSettings(
                theme = theme,
                dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
                defaultWastePercent = prefs[KEY_DEFAULT_WASTE] ?: 10.0,
                defaultAreaUnit = areaUnit,
                defaultTileUnit = tileUnit,
                hapticFeedbackEnabled = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
                soundEffectsEnabled = prefs[KEY_SOUND_EFFECTS] ?: false
            )
        }

    val historyFlow: Flow<List<CalculationHistoryItem>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val json = prefs[KEY_HISTORY] ?: ""
            CalculationHistoryJson.listFromJson(json)
        }

    suspend fun saveCalculation(item: CalculationHistoryItem) {
        context.dataStore.edit { prefs ->
            val existingJson = prefs[KEY_HISTORY] ?: ""
            val currentList = CalculationHistoryJson.listFromJson(existingJson).toMutableList()

            // Prepend new calculation so newest is first
            currentList.add(0, item)

            // Limit to max 50 items (dropping oldest entries)
            val trimmedList = if (currentList.size > MAX_HISTORY_ITEMS) {
                currentList.take(MAX_HISTORY_ITEMS)
            } else {
                currentList
            }

            prefs[KEY_HISTORY] = CalculationHistoryJson.listToJson(trimmedList)
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.dataStore.edit { prefs ->
            val existingJson = prefs[KEY_HISTORY] ?: ""
            val currentList = CalculationHistoryJson.listFromJson(existingJson)
            val updatedList = currentList.filter { it.id != id }
            prefs[KEY_HISTORY] = CalculationHistoryJson.listToJson(updatedList)
        }
    }

    suspend fun clearAllHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_HISTORY)
        }
    }

    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateDefaultWaste(percent: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_WASTE] = percent
        }
    }

    suspend fun updateDefaultAreaUnit(unit: MeasurementUnit) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_AREA_UNIT] = unit.name
        }
    }

    suspend fun updateDefaultTileUnit(unit: MeasurementUnit) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_TILE_UNIT] = unit.name
        }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun updateSoundEffects(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_EFFECTS] = enabled
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_THEME)
            prefs.remove(KEY_DYNAMIC_COLOR)
            prefs.remove(KEY_DEFAULT_WASTE)
            prefs.remove(KEY_DEFAULT_AREA_UNIT)
            prefs.remove(KEY_DEFAULT_TILE_UNIT)
            prefs.remove(KEY_HAPTIC_FEEDBACK)
            prefs.remove(KEY_SOUND_EFFECTS)
        }
    }
}
