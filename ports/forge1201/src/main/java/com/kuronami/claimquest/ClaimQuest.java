package com.kuronami.claimquest;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Claim Quest — registers a "Claim Chunks" task type for FTB Quests whose
 * progress is driven by FTB Chunks claim events.
 *
 * <p>Every FTB-facing class is loaded only behind a {@code ModList} guard in
 * {@link #commonSetup}, so the jar also loads in packs that dropped either mod
 * (it then registers nothing and stays inert — mods.toml declares both as
 * optional).
 */
@Mod(ClaimQuest.MODID)
public class ClaimQuest {

    public static final String MODID = "claimquest";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ClaimQuest() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (!net.minecraftforge.fml.ModList.get().isLoaded("ftbquests")) {
            LOGGER.info("FTB Quests not present; claim task type will not be registered");
            return;
        }
        // TaskTypes.TYPES is a plain map — keep registration on the main thread.
        event.enqueueWork(() -> {
            ClaimQuestTaskTypes.register();
            if (net.minecraftforge.fml.ModList.get().isLoaded("ftbchunks")) {
                ClaimProgressHook.register();
                LOGGER.info("Claim Quest: task type registered, claim hook live");
            } else {
                LOGGER.info("FTB Chunks not present; claim task type registered but will not progress");
            }
        });
    }
}
