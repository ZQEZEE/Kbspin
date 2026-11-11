package com.example.kbspin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class KbSpinClient implements ClientModInitializer {
    /** Угол для кнопок влево/вправо. */
    private static final float SIDE_ANGLE = 90.0f;
    /** Первый полуоборот для режима 360 (второй полуоборот возвращает камеру). */
    private static final float HALF_TURN = 180.0f;

    private static KeyBinding leftKey;
    private static KeyBinding rightKey;
    private static KeyBinding fullKey;

    private int phase = 0; // 0 = ждём, 1 = повёрнуто, бьём на следующем тике
    private float startYaw;
    private Entity target;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("kbspin", "main"));

        leftKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "KB Spin Left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, category));
        rightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "KB Spin Right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, category));
        fullKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "KB Spin 360", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            phase = 0;
            return;
        }

        if (phase == 0) {
            boolean left = leftKey.wasPressed();
            boolean right = rightKey.wasPressed();
            boolean full = fullKey.wasPressed();
            if (left || right || full) {
                // запоминаем цель до поворота камеры
                if (client.crosshairTarget instanceof EntityHitResult hit) {
                    target = hit.getEntity();
                } else {
                    target = null;
                }
                startYaw = player.getYaw();
                // в Minecraft yaw растёт при повороте вправо
                float offset = full ? HALF_TURN : (right ? SIDE_ANGLE : -SIDE_ANGLE);
                player.setYaw(startYaw + offset);
                phase = 1;
            }
            return;
        }

        // тик 2: бьём цель и возвращаем камеру (для 360 это второй полуоборот)
        if (target != null && target.isAlive()) {
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
        player.setYaw(startYaw);
        phase = 0;
    }
}
