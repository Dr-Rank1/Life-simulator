package com.maisha.game.data

import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.HustleType
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.domain.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobPoolTest {

    @Test
    fun `every job id is unique and has a title and salary`() {
        val ids = JobPool.jobs.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        JobPool.jobs.forEach { job ->
            assertTrue(job.id.isNotBlank())
            assertTrue(job.title.isNotBlank())
            assertTrue("${job.id} salary", job.baseSalary > 0)
        }
    }

    @Test
    fun `every roster country has distinctive local jobs`() {
        CountryCatalog.all().forEach { country ->
            assertTrue(
                "${country.code} should have local jobs",
                JobPool.hasCountryFlavorJobs(country.code)
            )
            val jobs = JobPool.getJobsForCountry(country.code)
            val localIds = jobs.map { it.id }.toSet() - universalIds()
            assertTrue("${country.code} local count ${localIds.size}", localIds.size >= 3)
            assertTrue(jobs.any { it.id == "teacher" })
        }
    }

    @Test
    fun `findById is stable for historic ids and strips visa suffix`() {
        assertEquals("Matatu Conductor", JobPool.findById("matatu_conductor")?.title)
        assertEquals("Software Developer", JobPool.findById("software_developer")?.title)
        assertEquals(
            JobPool.findById("teacher"),
            JobPool.findById("teacher@GB")
        )
    }

    @Test
    fun `Kenya board lists local jobs before the universal catalog`() {
        val kenya = JobPool.getJobsForCountry("KE")
        assertEquals("matatu_conductor", kenya.first().id)
        assertTrue(kenya.indexOfFirst { it.id == "matatu_conductor" } < kenya.indexOfFirst { it.id == "teacher" })
    }

    @Test
    fun `food delivery is open to older teens`() {
        val teen = TestFixtures.character(age = 16)
        assertTrue(JobPool.meetsSideHustlePrerequisites(teen, HustleType.FOOD_DELIVERY))
        assertFalse(JobPool.meetsSideHustlePrerequisites(teen, HustleType.TUTORING))
    }

    @Test
    fun `ride share requires a vehicle`() {
        val walker = TestFixtures.character(age = 22)
        val driver = TestFixtures.character(
            age = 22,
            assets = listOf(TestFixtures.asset(type = AssetType.CAR))
        )
        assertFalse(JobPool.meetsSideHustlePrerequisites(walker, HustleType.RIDE_SHARE))
        assertTrue(JobPool.meetsSideHustlePrerequisites(driver, HustleType.RIDE_SHARE))
    }

    @Test
    fun `historic transport jobs remain on their countries`() {
        assertTrue(JobPool.getJobsForCountry("KE").any { it.id == "matatu_conductor" })
        assertTrue(JobPool.getJobsForCountry("NG").any { it.id == "danfo_conductor" })
        assertTrue(JobPool.getJobsForCountry("PH").any { it.id == "jeepney_driver" })
        assertTrue(JobPool.getJobsForCountry("IN").any { it.id == "auto_rickshaw_driver" })
        assertTrue(JobPool.getJobsForCountry("ID").any { it.id == "angkot_driver" })
        assertTrue(JobPool.getJobsForCountry("BR").any { it.id == "mototaxi_rider" })
        assertTrue(JobPool.getJobsForCountry("ZA").any { it.id == "minibus_taxi_conductor" })
        assertTrue(JobPool.getJobsForCountry("EG").any { it.id == "microbus_driver" })
    }

    @Test
    fun `military and creator jobs keep their gates`() {
        val private = JobPool.findById("military_private")!!
        assertTrue(private.isMilitary)
        assertEquals(SchoolStage.NONE, private.minEducation)
        assertEquals(50_000, JobPool.findById("brand_ambassador")?.minFollowers)
        assertTrue(JobPool.findById("delivery_driver")!!.requiresDrivingLicense)
    }

    private fun universalIds(): Set<String> =
        JobPool.getJobsForCountry("ZZ").map { it.id }.toSet()
}
