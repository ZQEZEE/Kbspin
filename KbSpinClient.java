package com.example.kbspin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KbSpinClient implements ClientModInitializer {
    /** За сколько тиков камера делает полный оборот и возвращается (1-2). */
    private static final int SPIN_TICKS = 2;

    private static KeyBinding spinKey;

    private boolean spinning = false;
    private int tickIndex = 0;
    private float startYaw;

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
        if (player == null) {
            spinning = false;
            return;
        }

        if (!spinning) {
            if (spinKey.wasPressed()) {
                spinning = true;
                tickIndex = 0;
                startYaw = player.getYaw();
            }
            return;
        }

        tickIndex++;
        if (tickIndex >= SPIN_TICKS) {
            // последний тик: ровно возвращаем исходный угол
            player.setYaw(startYaw);
            spinning = false;
        } else {
            player.setYaw(startYaw + (360.0f / SPIN_TICKS) * tickIndex);
        }
    }
}
