package com.dimaskama.orthocamera.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.ModConfigSpec;

public class OrthoCameraConfig {
    protected static final double MIN_SCALE_D = 0.01d;
    protected static final float MIN_SCALE_F = (float) MIN_SCALE_D;
    protected static final float MAX_SCALE_F = 10000.0f;

    protected final ModConfigSpec.BooleanValue ENABLED;
    protected final ModConfigSpec.BooleanValue SAVE_ENABLED_STATE;
    protected final ModConfigSpec.DoubleValue SCALE_X;
    protected final ModConfigSpec.DoubleValue SCALE_Y;
    protected final ModConfigSpec.DoubleValue MIN_DISTANCE;
    protected final ModConfigSpec.DoubleValue MAX_DISTANCE;
    protected final ModConfigSpec.BooleanValue FIXED;
    protected final ModConfigSpec.DoubleValue FIXED_YAW;
    protected final ModConfigSpec.DoubleValue FIXED_PITCH;
    protected final ModConfigSpec.DoubleValue FIXED_ROTATE_SPEED_X;
    protected final ModConfigSpec.DoubleValue FIXED_ROTATE_SPEED_Y;
    protected final ModConfigSpec.BooleanValue AUTO_THIRD_PERSON;

    protected transient boolean dirty;
    protected transient float prevScaleX;
    protected transient float prevScaleY;
    protected transient float prevFixedYaw;
    protected transient float prevFixedPitch;
    protected transient CameraType prevPerspective;

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

    public OrthoCameraConfig(ModConfigSpec.Builder builder) {
        ENABLED = builder.translation("orthocamera.config.enabled")
                .define("enabled", false);

        SAVE_ENABLED_STATE = builder.translation("orthocamera.config.save_enabled_state")
                .define("save_enabled_state", false);

        SCALE_X = builder.translation("orthocamera.config.scale_x")
                .defineInRange("scale_x", 3, MIN_SCALE_D, MAX_SCALE_F);

        SCALE_Y = builder.translation("orthocamera.config.scale_y")
                .defineInRange("scale_y", 3, MIN_SCALE_D, MAX_SCALE_F);

        MIN_DISTANCE = builder.translation("orthocamera.config.min_distance")
                .defineInRange("min_distance", -1000f, -1000, 0);

        MAX_DISTANCE = builder.translation("orthocamera.config.max_distance")
                .defineInRange("max_distance", 1000f, 0, 1000);

        FIXED = builder.translation("orthocamera.config.fixed")
                .define("fixed", false);

        FIXED_YAW = builder.translation("orthocamera.config.fixed_yaw")
                .defineInRange("fixed_yaw", 0f, 0, 360);

        FIXED_PITCH = builder.translation("orthocamera.config.fixed_pitch")
                .defineInRange("fixed_pitch", 0f, -90, 90);

        FIXED_ROTATE_SPEED_X = builder.translation("orthocamera.config.fixed_rotate_speed_y")
                .defineInRange("fixed_rotate_speed_x", 3f, 0, 90);

        FIXED_ROTATE_SPEED_Y = builder.translation("orthocamera.config.fixed_rotate_speed_x")
                .defineInRange("fixed_rotate_speed_y", 3f, 0, 90);

        AUTO_THIRD_PERSON = builder.translation("orthocamera.config.auto_third_person")
                .define("auto_third_person", true);
    }

    public void load() {
        enabled = ENABLED.get();
        save_enabled_state = SAVE_ENABLED_STATE.get();
        scale_x = SCALE_X.get().floatValue();
        scale_y = SCALE_Y.get().floatValue();
        min_distance = MIN_DISTANCE.get().floatValue();
        max_distance = MAX_DISTANCE.get().floatValue();
        fixed = FIXED.get();
        fixed_yaw = FIXED_YAW.get().floatValue();
        fixed_pitch = FIXED_PITCH.get().floatValue();
        fixed_rotate_speed_x = FIXED_ROTATE_SPEED_X.get().floatValue();
        fixed_rotate_speed_y = FIXED_ROTATE_SPEED_Y.get().floatValue();
        auto_third_person = AUTO_THIRD_PERSON.get();
    }

    public void save() {
        // TODO
    }

    public void saveIfDirty() {
        if (dirty) {
            save();
        }
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
        scale = Mth.clamp(scale, MIN_SCALE_F, MAX_SCALE_F);
        if (scale != scale_x) {
            scale_x = scale;
            setDirty(true);
        }
    }

    public void setScaleY(float scale) {
        scale = Mth.clamp(scale, MIN_SCALE_F, MAX_SCALE_F);
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
