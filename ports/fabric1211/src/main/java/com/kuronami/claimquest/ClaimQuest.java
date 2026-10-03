package com.kuronami.claimquest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fabric entry point. Optional FTB APIs stay behind loader presence checks. */
public final class ClaimQuest implements ModInitializer {

    public static final String MODID = "claimquest";
    public static final Logger LOGGER = LoggerFactory.getLogger("Claim Quest");

    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isModLoaded("ftbquests")) {
            LOGGER.info("FTB Quests not present; claim task type will not be registered");
            return;
        }

        FtbQuestsIntegration.register();
        if (FabricLoader.getInstance().isModLoaded("ftbchunks")) {
            FtbChunksIntegration.register();
            LOGGER.info("Claim Quest: task type registered, claim hook live");
        } else {
            LOGGER.info("FTB Chunks not present; task type registered but will not progress");
        }
    }
}
