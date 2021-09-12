package me.mrfunny.chatfilter;

import me.mrfunny.chatfilter.gui.ConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.http.util.LangUtils;
import org.lwjgl.input.Keyboard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Mod(modid = AA.MODID, version = AA.VERSION, name = "Chat Filter")
public class AA {
    public static final String MODID = "chatfilter";
    public static final String VERSION = "1.0";
    private KeyBinding toggleKeybind;
    private boolean enabled = false;
    private final Minecraft mc = Minecraft.getMinecraft();
    private final ArrayList<String> queue = new ArrayList<>();
    private long ticks = 0;
    private final ArrayList<Thread> activeThreads = new ArrayList<>();
    private boolean working = false;
    private final HashMap<String, Integer> amountOfParties = new HashMap<>();
    private KeyBinding settingsKeyBind;

    private final String[] messages = {
            "Wanna get skills levels fast? /p me",
            "I found insane tactics to farm skills XP! /p me",
            "Want to get skills levels fast? /p me ",
            "Party me if you want to get tactics to farm XP levels VERY fast. /p me",
            "I've found discord that teaches you how to get skills XP very fast /p me"
    };

    private final String[] discordMessages = {
            "/pc Hypixel Skyblock easy skill farming: " + Settings.discordLink,
            "/pc Just join this discord: " + Settings.discordLink,
            "/pc I've found it here -> " + Settings.discordLink,
            "/pc This is real, just join this discord -> " + Settings.discordLink
    };

    private Configuration configuration;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event){
        configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.renameProperty("general", "Discord", "Discord Link");
        syncConfig();
        if(configuration.hasChanged()){
            configuration.save();
        }
    }

    public void syncConfig(){
        Settings.discordLink = configuration.getString("Discord Link", "general", "https://discord.gg/yxzb4gc9qY", "Discord link which will be sent in invites");
    }

    public Configuration getConfig(){
        return configuration;
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        toggleKeybind = new KeyBinding("keybind.ps.toggle", Keyboard.KEY_J, "category.ps.partyspammer");
        settingsKeyBind = new KeyBinding("keybind.ps.settings", Keyboard.KEY_P, "category.ps.partyspammer");
        ClientRegistry.registerKeyBinding(settingsKeyBind);
        ClientRegistry.registerKeyBinding(toggleKeybind);
        MinecraftForge.EVENT_BUS.register(this);
    }

    boolean debug = true;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        boolean enabledNow = false;
        if(event.phase != TickEvent.Phase.END) return;
        if(toggleKeybind.isPressed()) {
            enabled = !enabled;
            enabledNow = enabled;
            mc.thePlayer.addChatMessage(new ChatComponentTranslation("state.ps." + (enabled ? "enabled" : "disabled")));
        } else if(settingsKeyBind.isPressed()){
            mc.displayGuiScreen(new ConfigGui(mc.currentScreen, this));
            return;
        }

        if(enabledNow){
            ticks = 0;
            sendAdvertisement(debug);
        }
        if(enabled){
            if(!working && !activeThreads.isEmpty()){
                for(Thread thread : activeThreads){
                    try {
                        thread.stop();
                    } catch (Exception ignored){}
                }
                activeThreads.clear();
            }
            if(ticks >= 800){
                ticks = 0;
                Thread thread = new Thread(() -> {
                    while(working){
                        System.out.println("Waiting");
                    }
                    working = true;
                    try {
                        mc.thePlayer.sendChatMessage("/play skyblock");
                        Thread.sleep(1500);
                        mc.thePlayer.sendChatMessage("/is");
                        Thread.sleep(1500);
                        mc.thePlayer.sendChatMessage("/hub");
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendAdvertisement(debug);
                    working = false;
                    Thread.currentThread().stop();
                });
                thread.start();
                activeThreads.add(thread);
            }
            ticks++;
        }
        if(!queue.isEmpty() && !working){
            new Thread(() -> {
                working = true;
                for(String player : queue){
                    spam(player);
                }
                queue.clear();
                working = false;
                Thread.currentThread().stop();
            }).start();
        }
    }

    @SubscribeEvent
    public void onParty(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        if(message.equals("Cannot send chat message")){
            sendAdvertisement(debug);
        }
        if(event.message.getUnformattedText().contains("has invited you to join their party!") && enabled){
            String[] partyData = message.split("\n");
            String filtered = partyData[1].replaceAll("\\[.+?]", "").replaceAll("has invited you to join their party!", "");
            filtered = (filtered.startsWith(" ") ? filtered : " " + filtered);
            if(amountOfParties.containsKey(filtered)){
                int currentAmount = amountOfParties.get(filtered);
                if(currentAmount >= 3){
                    mc.thePlayer.sendChatMessage("/ignore add " + filtered);
                    amountOfParties.remove(filtered);
                    return;
                } else {
                    amountOfParties.replace(filtered, currentAmount + 1);
                }
            } else {
                if(amountOfParties.size() > 10){
                    amountOfParties.remove(amountOfParties.keySet().stream().findFirst().get());
                }
                amountOfParties.put(filtered, 0);
            }
            if (working) {
                queue.add(filtered);
            } else {
                spamAsync(filtered);
            }
        }
    }

    private final Random random = new Random();
    public void sendAdvertisement(boolean debug){
        mc.thePlayer.sendChatMessage(messages[random.nextInt(messages.length)] + (debug ? " " + Settings.discordLink : ""));
    }

    public void spamAsync(String name){
        new Thread(() -> {
            spam(name);
            Thread.currentThread().stop();
        }).start();
    }

    public void spam(String name){
        working = true;
        mc.thePlayer.sendChatMessage("/p accept" + name);
        try {
            Thread.sleep(500);
            mc.thePlayer.sendChatMessage(discordMessages[random.nextInt(discordMessages.length)]);
            Thread.sleep(500);
            mc.thePlayer.sendChatMessage("/p leave");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        working = false;
    }
}