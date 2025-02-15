package com.dimaskama.orthocamera.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final float MIN_SCALE = 0.01F;
    public static final float MAX_SCALE = 10000.0F;

    private final ModConfigSpec.BooleanValue ENABLED;
    private final ModConfigSpec.BooleanValue SAVE_ENABLED_STATE;
    private final ModConfigSpec.ConfigValue<Float> SCALE_X;
    private final ModConfigSpec.ConfigValue<Float> SCALE_Y;
    private final ModConfigSpec.ConfigValue<Float> MIN_DISTANCE;
    private final ModConfigSpec.ConfigValue<Float> MAX_DISTANCE;
    private final ModConfigSpec.BooleanValue FIXED;
    private final ModConfigSpec.ConfigValue<Float> FIXED_YAW;
    private final ModConfigSpec.ConfigValue<Float> FIXED_PITCH;
    private final ModConfigSpec.ConfigValue<Float> FIXED_ROTATE_SPEED_X;
    private final ModConfigSpec.ConfigValue<Float> FIXED_ROTATE_SPEED_Y;
    private final ModConfigSpec.BooleanValue AUTO_THIRD_PERSON;

    private transient boolean dirty;
    private transient float prevScaleX;
    private transient float prevScaleY;
    private transient float prevFixedYaw;
    private transient float prevFixedPitch;
    private transient CameraType prevPerspective;

    public boolean enabled = false;
    public boolean save_enabled_state;
    public float scale_x = 3.0F;
    public float scale_y = 3.0F;
    public float min_distance = -1000.0F;
    public float max_distance = 1000.0F;
    public boolean fixed = false;
    public float fixed_yaw = 0.0F;
    public float fixed_pitch = 0.0F;
    public float fixed_rotate_speed_x = 3.0F;
    public float fixed_rotate_speed_y = 3.0F;
    public boolean auto_third_person = true;

    public ModConfig(ModConfigSpec.Builder builder) {
        ENABLED = builder.define("enabled", false);

        SAVE_ENABLED_STATE = builder.define("save_enabled_state", false);

        SCALE_X = builder.defineInRange("scale_x", 3f, MIN_SCALE, MAX_SCALE, Float.class);

        SCALE_Y = builder.defineInRange("scale_y", 3f, MIN_SCALE, MAX_SCALE, Float.class);

        MIN_DISTANCE = builder.defineInRange("min_distance", -1000f, -1000f, 0f, Float.class);

        MAX_DISTANCE = builder.defineInRange("max_distance", 1000f, 0f, 1000f, Float.class);

        FIXED = builder.define("fixed", false);

        FIXED_YAW = builder.defineInRange("fixed_yaw", 0f, 0f, 360f, Float.class);

        FIXED_PITCH = builder.defineInRange("fixed_pitch", 0f, -90f, 90f, Float.class);

        FIXED_ROTATE_SPEED_X = builder.defineInRange("fixed_rotate_speed_x", 0f, 0f, 90f, Float.class);

        FIXED_ROTATE_SPEED_Y = builder.defineInRange("fixed_rotate_speed_y", 0f, 0f, 90f, Float.class);

        AUTO_THIRD_PERSON = builder.define("auto_third_person", true);
    }

    void load() {
        enabled = ENABLED.get();
        save_enabled_state = SAVE_ENABLED_STATE.get();
        scale_x = SCALE_X.get();
        scale_y = SCALE_Y.get();
        min_distance = MIN_DISTANCE.get();
        max_distance = MAX_DISTANCE.get();
        fixed = FIXED.get();
        fixed_yaw = FIXED_YAW.get();
        fixed_pitch = FIXED_PITCH.get();
        fixed_rotate_speed_x = FIXED_ROTATE_SPEED_X.get();
        fixed_rotate_speed_y = FIXED_ROTATE_SPEED_Y.get();
        auto_third_person = AUTO_THIRD_PERSON.get();
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void tick() {
        prevScaleX = scale_x;
        prevScaleY = scale_y;
        prevFixedYaw = fixed_yaw;
        prevFixedPitch = fixed_pitch;
    }

    public float getScaleX(float delta) {
        return Mth.lerp(delta, prevScaleX, scale_x);
    }

    public float getScaleY(float delta) {
        return Mth.lerp(delta, prevScaleY, scale_y);
    }

    public float getFixedYaw(float delta) {
        return Mth.rotLerp(delta, prevFixedYaw, fixed_yaw);
    }

    public float getFixedPitch(float delta) {
        return Mth.rotLerp(delta, prevFixedPitch, fixed_pitch);
    }

    public void setScaleX(float scale) {
        scale = Mth.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_x) {
            scale_x = scale;
            setDirty(true);
        }
    }

    public void setScaleY(float scale) {
        scale = Mth.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_y) {
            scale_y = scale;
            setDirty(true);
        }
    }

    public void setFixedYaw(float yaw) {
        if (yaw < 0) yaw = 360 + yaw;
        yaw = yaw % 360;
        if (yaw != fixed_yaw) {
            fixed_yaw = yaw;
            setDirty(true);
        }
    }

    public void setFixedPitch(float pitch) {
        pitch = Mth.clamp(pitch, -90.0F, 90.0F);
        if (pitch != fixed_pitch) {
            fixed_pitch = pitch;
            setDirty(true);
        }
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
        if (fixed) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity != null) {
                setFixedYaw(entity.getYRot() + 180);
                prevFixedYaw = fixed_yaw;
                setFixedPitch(entity.getXRot());
                prevFixedPitch = fixed_pitch;
            }
        }
        setDirty(true);
    }

    public void toggle() {
        enabled = !enabled;
        if (auto_third_person) {
            Minecraft client = Minecraft.getInstance();
            if (enabled) {
                prevPerspective = client.options.getCameraType();
                client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else if (prevPerspective != null) {
                client.options.setCameraType(prevPerspective);
            }
        }
        setDirty(true);
    }
}
