package me.mrfunny.chatfilter.gui;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.IModGuiFactory;

public class GuiFactory implements IModGuiFactory {
    public void initialize(Minecraft minecraftInstance) {
    }

    public Class mainConfigGuiClass() {
        return ConfigGui.class;
    }

    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public IModGuiFactory.RuntimeOptionGuiHandler getHandlerFor(IModGuiFactory.RuntimeOptionCategoryElement element) {
        return null;
    }
}
