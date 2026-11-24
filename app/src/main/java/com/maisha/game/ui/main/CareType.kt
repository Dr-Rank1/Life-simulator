// app/src/main/java/com/maisha/game/ui/main/CareType.kt
package com.maisha.game.ui.main

import android.content.res.Resources
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.model.CareTier

typealias CareType = CareTier

fun CareTier.displayName(res: Resources, countryCode: String = "XX"): String = when (this) {
    CareTier.PUBLIC_CLINIC -> res.getString(R.string.care_public_clinic)
    CareTier.PRIVATE_HOSPITAL -> {
        val name = CountryCatalog.flavorFor(countryCode).privateHospitalName
        res.getString(R.string.care_private_hospital, name)
    }
    CareTier.ALTERNATIVE_MEDICINE -> res.getString(R.string.care_alternative_medicine)
    CareTier.MENTAL_WELLNESS -> res.getString(R.string.care_mental_wellness)
}
