package com.kuronami.claimquest;

/** Keeps FTB Quests references isolated when that optional mod is absent. */
final class FtbQuestsIntegration {

    private FtbQuestsIntegration() {
    }

    static void register() {
        ClaimQuestTaskTypes.register();
    }
}
