package com.nekogan.pipez.integration.jei;

import com.nekogan.pipez.Main;
import com.nekogan.pipez.gui.ExtractScreen;
import com.nekogan.pipez.gui.FilterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Main.MODID, "pipez_ce");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(ExtractScreen.class, new ExtractScreenHandler());
        registration.addGhostIngredientHandler(FilterScreen.class, new FilterScreenGhostIngredientHandler());
    }
}
