package com.nekogan.pipez;

import de.maxhenkel.corelib.CommonRegistry;
import com.nekogan.pipez.blocks.ModBlocks;
import com.nekogan.pipez.blocks.tileentity.ModTileEntities;
import com.nekogan.pipez.events.BlockEvents;
import com.nekogan.pipez.gui.Containers;
import com.nekogan.pipez.integration.IMC;
import com.nekogan.pipez.items.ModItems;
import com.nekogan.pipez.net.*;
import com.nekogan.pipez.recipes.ModRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.resource.PathPackResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Main.MODID)
public class Main {

    public static final String MODID = "pipez_ce";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    public static SimpleChannel SIMPLE_CHANNEL;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(IMC::enqueueIMC);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class);
        CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.CLIENT, ClientConfig.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ModelRegistry::onModelRegister);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ModelRegistry::onModelBake);
        });

        ModBlocks.init();
        ModItems.init();
        ModRecipes.init();
        Containers.init();
        ModTileEntities.init();
        ModCreativeTabs.init();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new BlockEvents());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.addListener(Main::onAddPackFinders);
        });

        SIMPLE_CHANNEL = CommonRegistry.registerChannel(Main.MODID, "default");
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 0, CycleDistributionMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 1, CycleRedstoneModeMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 2, CycleFilterModeMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 3, UpdateFilterMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 4, RemoveFilterMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 5, EditFilterMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 6, OpenExtractMessage.class);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        ModTileEntities.clientSetup();
        Containers.clientSetup();
    }

    @OnlyIn(Dist.CLIENT)
    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        try {
            var modFile = ModList.get().getModFileById(MODID);
            var resourcePath = modFile.getFile().findResource("resourcepacks/pipez_ce_classic");
            if (resourcePath == null) return;

            var pack = Pack.readMetaAndCreate(
                    "pipez_ce_classic",
                    net.minecraft.network.chat.Component.literal("Pipez CE Classic Textures"),
                    false,
                    name -> new PathPackResources("pipez_ce_classic", true, resourcePath),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.BOTTOM,
                    PackSource.DEFAULT
            );
            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to register classic resource pack", e);
        }
    }

}
