package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.di.createDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class PreferenceRepositoryTest {

    private fun createTestRepository(): PreferenceRepository {
        val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test_${Uuid.random().toHexString()}.preferences_pb"
        val dataStore = createDataStore { tempPath.toString() }
        return PreferenceRepository(dataStore)
    }

    @Test
    fun testKwhenConfigDefaults() = runBlocking {
        assertEquals("kwhen_morning_hour", PreferenceRepository.KWHEN_MORNING_HOUR)
        assertEquals("kwhen_afternoon_hour", PreferenceRepository.KWHEN_AFTERNOON_HOUR)
        assertEquals("kwhen_evening_hour", PreferenceRepository.KWHEN_EVENING_HOUR)
        assertEquals("kwhen_night_hour", PreferenceRepository.KWHEN_NIGHT_HOUR)
        assertEquals("kwhen_use_24", PreferenceRepository.KWHEN_USE_24)
        assertEquals("kwhen_use_european_date", PreferenceRepository.KWHEN_USE_EUROPEAN_DATE)

        assertEquals(9, PreferenceRepository.intPreference[PreferenceRepository.KWHEN_MORNING_HOUR])
        assertEquals(15, PreferenceRepository.intPreference[PreferenceRepository.KWHEN_AFTERNOON_HOUR])
        assertEquals(18, PreferenceRepository.intPreference[PreferenceRepository.KWHEN_EVENING_HOUR])
        assertEquals(20, PreferenceRepository.intPreference[PreferenceRepository.KWHEN_NIGHT_HOUR])
        assertEquals(false, PreferenceRepository.boolPreference[PreferenceRepository.KWHEN_USE_24])
        assertEquals(false, PreferenceRepository.boolPreference[PreferenceRepository.KWHEN_USE_EUROPEAN_DATE])

        val repo = createTestRepository()
        val config = repo.getKwhenConfig().first()
        assertEquals(9, config.morning)
        assertEquals(15, config.afternoon)
        assertEquals(18, config.evening)
        assertEquals(20, config.night)
        assertFalse(config.use24)
        assertFalse(config.useEuropeanDate)
    }

    @Test
    fun testUpdateKwhenConfig() = runBlocking {
        val repo = createTestRepository()

        repo.setMorningHour(8)
        repo.setAfternoonHour(14)
        repo.setEveningHour(19)
        repo.setNightHour(22)
        repo.setUse24Hour(true)
        repo.setUseEuropeanDate(true)

        val config = repo.getKwhenConfig().first()
        assertEquals(8, config.morning)
        assertEquals(14, config.afternoon)
        assertEquals(19, config.evening)
        assertEquals(22, config.night)
        assertTrue(config.use24)
        assertTrue(config.useEuropeanDate)
    }

    @Test
    fun testResetKwhenConfigToDefaults() = runBlocking {
        val repo = createTestRepository()

        repo.setMorningHour(7)
        repo.setAfternoonHour(13)
        repo.setEveningHour(17)
        repo.setNightHour(23)
        repo.setUse24Hour(true)
        repo.setUseEuropeanDate(true)

        var config = repo.getKwhenConfig().first()
        assertEquals(7, config.morning)
        assertTrue(config.use24)

        repo.resetKwhenConfigToDefaults()

        config = repo.getKwhenConfig().first()
        assertEquals(9, config.morning)
        assertEquals(15, config.afternoon)
        assertEquals(18, config.evening)
        assertEquals(20, config.night)
        assertFalse(config.use24)
        assertFalse(config.useEuropeanDate)
    }
}
