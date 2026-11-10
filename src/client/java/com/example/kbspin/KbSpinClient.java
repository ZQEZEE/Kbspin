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
    /** На сколько градусов повернуть камеру перед ударом (180 = отбросит назад). */
    private static final float ANGLE = 180.0f;

    private static KeyBinding spinKey;

    private int phase = 0; // 0 = ждём, 1 = повёрнуто, ждём следующий тик
    private float startYaw;
    private Entity target;

    @Override
    public void onInitializeClient() {
        spinKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kbspin.spin",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyBinding.Category.create(Identifier.of("kbspin", "main"))
        ));
        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            phase = 0;
            return;
        }

        if (phase == 0) {
            if (spinKey.wasPressed()) {
                // запоминаем цель до поворота камеры
                if (client.crosshairTarget instanceof EntityHitResult hit) {
                    target = hit.getEntity();
                } else {
                    target = null;
                }
                startYaw = player.getYaw();
                player.setYaw(startYaw + ANGLE); // тик 1: камера повёрнута
                phase = 1;
            }
            return;
        }

        // тик 2: бьём цель и возвращаем камеру
        if (target != null && target.isAlive()) {
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
        player.setYaw(startYaw);
        phase = 0;
    }
}
