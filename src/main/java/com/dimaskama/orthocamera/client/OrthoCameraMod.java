package com.dimaskama.orthocamera.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(value = OrthoCameraMod.MOD_ID, dist = Dist.CLIENT)
public class OrthoCameraMod {
    public static final String MOD_ID = "orthocamera";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final OrthoCameraConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;
    static {
        Pair<OrthoCameraConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(OrthoCameraConfig::new);    // TODO this is gross, switch to how I did it in Stackcraft
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private static final KeyMapping TOGGLE_KEY = createKeybinding("toggle", GLFW.GLFW_KEY_KP_4);
    private static final KeyMapping SCALE_INCREASE_KEY = createKeybinding("scale_increase", GLFW.GLFW_KEY_KP_SUBTRACT);
    private static final KeyMapping SCALE_DECREASE_KEY = createKeybinding("scale_decrease", GLFW.GLFW_KEY_KP_ADD);
    private static final KeyMapping OPEN_OPTIONS_KEY = createKeybinding("options", -1);
    private static final KeyMapping FIX_CAMERA_KEY = createKeybinding("fix_camera", GLFW.GLFW_KEY_KP_MULTIPLY);
    private static final KeyMapping FIXED_CAMERA_ROTATE_UP_KEY = createKeybinding("fixed_camera_rotate_up", -1);
    private static final KeyMapping FIXED_CAMERA_ROTATE_DOWN_KEY = createKeybinding("fixed_camera_rotate_down", -1);
    private static final KeyMapping FIXED_CAMERA_ROTATE_LEFT_KEY = createKeybinding("fixed_camera_rotate_left", -1);
    private static final KeyMapping FIXED_CAMERA_ROTATE_RIGHT_KEY = createKeybinding("fixed_camera_rotate_right", -1);
    private static final Component ENABLED_TEXT = Component.translatable("orthocamera.enabled");
    private static final Component DISABLED_TEXT = Component.translatable("orthocamera.disabled");
    private static final Component FIXED_TEXT = Component.translatable("orthocamera.fixed");
    private static final Component UNFIXED_TEXT = Component.translatable("orthocamera.unfixed");
    private static final float SCALE_MUL_INTERVAL = 1.1F;

    public OrthoCameraMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onModConfig);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.CLIENT, CONFIG_SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public void onModConfig(ModConfigEvent event) {
        CONFIG.load();
    }

    @SubscribeEvent
    public void onClientTickPre(ClientTickEvent.Pre event) {
        CONFIG.tick();
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {   // TODO need to save config if dirty on client disconnect/world close
        CONFIG.save();
    }

//    @Override
//    public void onInitializeClient() {
//        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
//        KeyBindingHelper.registerKeyBinding(SCALE_INCREASE_KEY);
//        KeyBindingHelper.registerKeyBinding(SCALE_DECREASE_KEY);
//        KeyBindingHelper.registerKeyBinding(OPEN_OPTIONS_KEY);
//        KeyBindingHelper.registerKeyBinding(FIX_CAMERA_KEY);
//        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_UP_KEY);
//        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_DOWN_KEY);
//        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_LEFT_KEY);
//        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_RIGHT_KEY);
//        ClientTickEvents.END_CLIENT_TICK.register(this::handleInput);
//        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
//    }
//
//    private void handleInput(Minecraft minecraft) {
//        boolean messageSent = false;
//        while (TOGGLE_KEY.wasPressed()) {
//            CONFIG.toggle();
//            minecraft.getMessageHandler().onGameMessage(
//                    CONFIG.enabled ? ENABLED_TEXT : DISABLED_TEXT,
//                    true
//            );
//            messageSent = true;
//        }
//        boolean on = CONFIG.enabled;
//        boolean scaleChanged = false;
//        while (SCALE_INCREASE_KEY.wasPressed()) {
//            if (on) {
//                CONFIG.setScaleX(CONFIG.scale_x * SCALE_MUL_INTERVAL);
//                CONFIG.setScaleY(CONFIG.scale_y * SCALE_MUL_INTERVAL);
//                CONFIG.setDirty(true);
//                scaleChanged = true;
//            }
//        }
//        while (SCALE_DECREASE_KEY.wasPressed()) {
//            if (on) {
//                CONFIG.setScaleX(CONFIG.scale_x / SCALE_MUL_INTERVAL);
//                CONFIG.setScaleY(CONFIG.scale_y / SCALE_MUL_INTERVAL);
//                CONFIG.setDirty(true);
//                scaleChanged = true;
//            }
//        }
//        if (scaleChanged && !messageSent) {
//            minecraft.getMessageHandler().onGameMessage(
//                    Component.translatable(
//                            "orthocamera.scale",
//                            String.format("%.1f", CONFIG.scale_x), String.format("%.1f", CONFIG.scale_y)
//                    ),
//                    true
//            );
//            messageSent = true;
//        }
//        boolean fixPressed = false;
//        while (FIX_CAMERA_KEY.wasPressed()) {
//            fixPressed = true;
//            CONFIG.setFixed(!CONFIG.fixed);
//        }
//        if (!messageSent && fixPressed) {
//            minecraft.getMessageHandler().onGameMessage(CONFIG.fixed ? FIXED_TEXT : UNFIXED_TEXT, true);
//        }
//        if (FIXED_CAMERA_ROTATE_LEFT_KEY.isPressed()) {
//            CONFIG.setFixedYaw(CONFIG.fixed_yaw + CONFIG.fixed_rotate_speed_y);
//        }
//        if (FIXED_CAMERA_ROTATE_RIGHT_KEY.isPressed()) {
//            CONFIG.setFixedYaw(CONFIG.fixed_yaw - CONFIG.fixed_rotate_speed_y);
//        }
//        if (FIXED_CAMERA_ROTATE_UP_KEY.isPressed()) {
//            CONFIG.setFixedPitch(CONFIG.fixed_pitch + CONFIG.fixed_rotate_speed_x);
//        }
//        if (FIXED_CAMERA_ROTATE_DOWN_KEY.isPressed()) {
//            CONFIG.setFixedPitch(CONFIG.fixed_pitch - CONFIG.fixed_rotate_speed_x);
//        }
//        boolean openScreen = false;
//        while (OPEN_OPTIONS_KEY.wasPressed()) {
//            openScreen = true;
//        }
//        if (openScreen) {
//            minecraft.setScreen(new ModConfigScreen(null));
//        }
//    }
//
//    private void onClientStopping(Minecraft minecraft) {
//        if (CONFIG.isDirty()) {
//            CONFIG.save();
//        }
//    }

    public static Matrix4f createOrthoMatrix(float delta, float minScale) { // TODO should I cache this?
        Minecraft minecraft = Minecraft.getInstance();
        float width = Math.max(minScale, CONFIG.getScaleX(delta)
                * minecraft.getWindow().getWidth() / minecraft.getWindow().getHeight());
        float height = Math.max(minScale, CONFIG.getScaleY(delta));
        return new Matrix4f().setOrtho(
                -width, width,
                -height, height,
                CONFIG.min_distance, CONFIG.max_distance
        );
    }

    private static KeyMapping createKeybinding(String name, int key) {  // TODO when some (all?) of the keys are pressed, need to save the new config values on disconnect
        return new KeyMapping(  // TODO confirm this works/is ok
                "orthocamera.key." + name,
                InputConstants.Type.KEYSYM,
                key,
                MOD_ID
        );
    }
}
