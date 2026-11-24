// app/src/main/java/com/maisha/game/domain/InteractionType.kt (modified — expanded interactions)
package com.maisha.game.domain

enum class InteractionType {
    SPEND_TIME,
    ARGUE,
    ASK_FOR_MONEY,
    GIFT,
    COMPLIMENT,
    INSULT,
    TRAVEL_TOGETHER,
    ASK_FOR_ADVICE,
    PRANK,
    SET_UP_ON_DATE,
    HELP_WITH_HOMEWORK,
    PAY_ALLOWANCE,
    DISCIPLINE,
    DATE_NIGHT,
    MAKE_PEACE,
    /** Adult child: cash support. */
    FINANCIAL_SUPPORT,
    /** Adult child: celebrate wedding, job, etc. */
    CELEBRATE_MILESTONE,
    /** Adult child: talk through life choices. */
    DISCUSS_LIFE_CHOICES,
    /** Sibling: team up on games, projects, or mischief (ages 4+). */
    SIBLING_TEAM_UP,
    /** Sibling: borrow or sneak a toy, clothes, or gadget (ages 6+). */
    SIBLING_BORROW_ITEM,
    /** Sibling: friendly or fierce rivalry competition (ages 5+). */
    SIBLING_COMPETE
}
