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
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
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
        Pair<OrthoCameraConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(OrthoCameraConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private static final Lazy<KeyMapping> TOGGLE_KEY = createLazyKeyMapping("toggle",
            GLFW.GLFW_KEY_KP_4);    // TODO bad key to use, doesn't seem to work
    private static final Lazy<KeyMapping> SCALE_INCREASE_KEY = createLazyKeyMapping("scale_increase",
            GLFW.GLFW_KEY_KP_SUBTRACT);
    private static final Lazy<KeyMapping> SCALE_DECREASE_KEY = createLazyKeyMapping("scale_decrease",
            GLFW.GLFW_KEY_KP_ADD);
    private static final Lazy<KeyMapping> OPEN_OPTIONS_KEY = createLazyKeyMapping("options",
            InputConstants.UNKNOWN.getValue());
    private static final Lazy<KeyMapping> FIX_CAMERA_KEY = createLazyKeyMapping("fix_camera",
            GLFW.GLFW_KEY_KP_MULTIPLY);
    private static final Lazy<KeyMapping> FIXED_CAMERA_ROTATE_UP_KEY = createLazyKeyMapping("fixed_camera_rotate_up",
            InputConstants.UNKNOWN.getValue());
    private static final Lazy<KeyMapping> FIXED_CAMERA_ROTATE_DOWN_KEY = createLazyKeyMapping("fixed_camera_rotate_down",
            InputConstants.UNKNOWN.getValue());
    private static final Lazy<KeyMapping> FIXED_CAMERA_ROTATE_LEFT_KEY = createLazyKeyMapping("fixed_camera_rotate_left",
            InputConstants.UNKNOWN.getValue());
    private static final Lazy<KeyMapping> FIXED_CAMERA_ROTATE_RIGHT_KEY = createLazyKeyMapping("fixed_camera_rotate_right",
            InputConstants.UNKNOWN.getValue());

    private static final Component ENABLED_TEXT = Component.translatable("orthocamera.enabled");
    private static final Component DISABLED_TEXT = Component.translatable("orthocamera.disabled");
    private static final Component FIXED_TEXT = Component.translatable("orthocamera.fixed");
    private static final Component UNFIXED_TEXT = Component.translatable("orthocamera.unfixed");
    private static final float SCALE_MUL_INTERVAL = 1.1F;

    public OrthoCameraMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onModConfig);
        modEventBus.addListener(this::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.CLIENT, CONFIG_SPEC);    // TODO would be nice if values changed in real time when changing them in config screen
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public void onModConfig(ModConfigEvent event) {
        CONFIG.saveIfDirty();
        CONFIG.load();
    }

    public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY.get());
        event.register(SCALE_INCREASE_KEY.get());
        event.register(SCALE_DECREASE_KEY.get());
        event.register(OPEN_OPTIONS_KEY.get());
        event.register(FIX_CAMERA_KEY.get());
        event.register(FIXED_CAMERA_ROTATE_UP_KEY.get());
        event.register(FIXED_CAMERA_ROTATE_DOWN_KEY.get());
        event.register(FIXED_CAMERA_ROTATE_LEFT_KEY.get());
        event.register(FIXED_CAMERA_ROTATE_RIGHT_KEY.get());
    }

    @SubscribeEvent
    public void onClientTickPre(ClientTickEvent.Pre event) {
        CONFIG.tick();
    }

    @SubscribeEvent
    public void onClientTickPost(ClientTickEvent.Post event) {
        // TODO do keymapping things
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {   // TODO need to save config if dirty on client disconnect/world close
        // TODO this seems to be the right event, but this triggered even when joining a world. Maybe that's ok if we only save if dirty
        CONFIG.saveIfDirty();
    }

//    @Override
//    public void onInitializeClient() {
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

    private static KeyMapping createKeyMapping(String name, int key) {  // TODO when some (all?) of the keys are pressed, need to save the new config values on disconnect
        return new KeyMapping(
                "key." + MOD_ID + "." + name,
                InputConstants.Type.KEYSYM,
                key,
                "key.categories." + MOD_ID
        );
    }

    private static Lazy<KeyMapping> createLazyKeyMapping(String name, int key) {
        return Lazy.of(() -> createKeyMapping(name, key));
    }
}
