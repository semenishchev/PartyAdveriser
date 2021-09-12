package me.mrfunny.chatfilter.gui;

import me.mrfunny.chatfilter.AA;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ConfigGui extends GuiConfig {
    private final AA mod;

    public ConfigGui(GuiScreen parent, AA mod) {
        super(parent, (new ConfigElement(mod.getConfig()
                .getCategory("general"))).getChildElements(), AA.MODID, false, false, "PartyAdviser mod options");
        this.mod = mod;
        this.titleLine2 = GuiConfig.getAbridgedConfigPath(mod.getConfig().toString());
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        mod.getConfig().save();
        mod.syncConfig();
    }
}