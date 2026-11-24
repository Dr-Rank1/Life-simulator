package com.maisha.game.ui.components

import androidx.annotation.StringRes
import com.maisha.game.R

object InterviewStrings {
    @StringRes
    fun stringResFor(key: String): Int = when (key) {
        "interview_dialog_title" -> R.string.interview_dialog_title
        "interview_dialog_subtitle" -> R.string.interview_dialog_subtitle
        "interview_hired_headline" -> R.string.interview_hired_headline
        "interview_rejected_headline" -> R.string.interview_rejected_headline
        "interview_hired_body" -> R.string.interview_hired_body
        "interview_rejected_body" -> R.string.interview_rejected_body

        "interview_q_deadline_prompt" -> R.string.interview_q_deadline_prompt
        "interview_q_deadline_c1" -> R.string.interview_q_deadline_c1
        "interview_q_deadline_c1_fb" -> R.string.interview_q_deadline_c1_fb
        "interview_q_deadline_c2" -> R.string.interview_q_deadline_c2
        "interview_q_deadline_c2_fb" -> R.string.interview_q_deadline_c2_fb
        "interview_q_deadline_c3" -> R.string.interview_q_deadline_c3
        "interview_q_deadline_c3_fb" -> R.string.interview_q_deadline_c3_fb

        "interview_q_culture_prompt" -> R.string.interview_q_culture_prompt
        "interview_q_culture_c1" -> R.string.interview_q_culture_c1
        "interview_q_culture_c1_fb" -> R.string.interview_q_culture_c1_fb
        "interview_q_culture_c2" -> R.string.interview_q_culture_c2
        "interview_q_culture_c2_fb" -> R.string.interview_q_culture_c2_fb
        "interview_q_culture_c3" -> R.string.interview_q_culture_c3
        "interview_q_culture_c3_fb" -> R.string.interview_q_culture_c3_fb

        "interview_q_tech_prompt" -> R.string.interview_q_tech_prompt
        "interview_q_tech_c1" -> R.string.interview_q_tech_c1
        "interview_q_tech_c1_fb" -> R.string.interview_q_tech_c1_fb
        "interview_q_tech_c2" -> R.string.interview_q_tech_c2
        "interview_q_tech_c2_fb" -> R.string.interview_q_tech_c2_fb
        "interview_q_tech_c3" -> R.string.interview_q_tech_c3
        "interview_q_tech_c3_fb" -> R.string.interview_q_tech_c3_fb

        "interview_q_ethics_prompt" -> R.string.interview_q_ethics_prompt
        "interview_q_ethics_c1" -> R.string.interview_q_ethics_c1
        "interview_q_ethics_c1_fb" -> R.string.interview_q_ethics_c1_fb
        "interview_q_ethics_c2" -> R.string.interview_q_ethics_c2
        "interview_q_ethics_c2_fb" -> R.string.interview_q_ethics_c2_fb
        "interview_q_ethics_c3" -> R.string.interview_q_ethics_c3
        "interview_q_ethics_c3_fb" -> R.string.interview_q_ethics_c3_fb

        "interview_q_medical_prompt" -> R.string.interview_q_medical_prompt
        "interview_q_medical_c1" -> R.string.interview_q_medical_c1
        "interview_q_medical_c1_fb" -> R.string.interview_q_medical_c1_fb
        "interview_q_medical_c2" -> R.string.interview_q_medical_c2
        "interview_q_medical_c2_fb" -> R.string.interview_q_medical_c2_fb
        "interview_q_medical_c3" -> R.string.interview_q_medical_c3
        "interview_q_medical_c3_fb" -> R.string.interview_q_medical_c3_fb

        "interview_q_service_prompt" -> R.string.interview_q_service_prompt
        "interview_q_service_c1" -> R.string.interview_q_service_c1
        "interview_q_service_c1_fb" -> R.string.interview_q_service_c1_fb
        "interview_q_service_c2" -> R.string.interview_q_service_c2
        "interview_q_service_c2_fb" -> R.string.interview_q_service_c2_fb
        "interview_q_service_c3" -> R.string.interview_q_service_c3
        "interview_q_service_c3_fb" -> R.string.interview_q_service_c3_fb

        else -> R.string.interview_dialog_title
    }
}
