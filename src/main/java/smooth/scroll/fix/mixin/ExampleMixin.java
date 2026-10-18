package smooth.scroll.fix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smooth.scroll.fix.SpeedSettings;

@Mixin(targets = "net.minecraft.client.player.LocalPlayer")
public class ExampleMixin {

    private static boolean upPressed = false;
    private static boolean downPressed = false;
    private static boolean leftPressed = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void modifyPlayer(CallbackInfo ci) {
        Object player = (Object) this;

        // ОБРАБОТКА НАЖАТИЙ КЛАВИШ (Управление скоростью без чата и команд)
        try {
            long window = (long) Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null)
                    .getClass().getField("window").get(Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null));
            
            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            java.lang.reflect.Method get_key = glfwClass.getMethod("glfwGetKey", long.class, int.class);

            int keyUp = glfwClass.getField("GLFW_KEY_UP").getInt(null);
            int keyDown = glfwClass.getField("GLFW_KEY_DOWN").getInt(null);
            int keyLeft = glfwClass.getField("GLFW_KEY_LEFT").getInt(null);

            // Нажата стрелочка ВВЕРХ (Скорость: 1.15 / 1.08)
            if ((int) get_key.invoke(null, window, keyUp) == 1) {
                if (!upPressed) {
                    SpeedSettings.walkMultiplier = 1.15;
                    SpeedSettings.waterMultiplier = 1.08;
                    SpeedSettings.sendMessage(player, "§a[Mod] Установлена стандартная скорость (Ходьба: 1.15, Вода: 1.08)");
                    upPressed = true;
                }
            } else { upPressed = false; }

            // Нажата стрелочка ВНИЗ (Скорость: 1.35 / 1.15)
            if ((int) get_key.invoke(null, window, keyDown) == 1) {
                if (!downPressed) {
                    SpeedSettings.walkMultiplier = 1.35;
                    SpeedSettings.waterMultiplier = 1.15;
                    SpeedSettings.sendMessage(player, "§b[Mod] Установлена быстрая скорость (Ходьба: 1.35, Вода: 1.15)");
                    downPressed = true;
                }
            } else { downPressed = false; }

            // Нажата стрелочка ВЛЕВО (Сброс на 1.0)
            if ((int) get_key.invoke(null, window, keyLeft) == 1) {
                if (!leftPressed) {
                    SpeedSettings.walkMultiplier = 1.0;
                    SpeedSettings.waterMultiplier = 1.0;
                    SpeedSettings.sendMessage(player, "§c[Mod] Модификаторы скорости сброшены на стандарт (1.0)");
                    leftPressed = true;
                }
            } else { leftPressed = false; }

        } catch (Exception ignored) {}


        // ПРИМЕНЕНИЕ СКОРОСТИ В ДВИЖЕНИИ (Mojmap блок)
        try {
            java.lang.reflect.Method isInWaterMethod = player.getClass().getMethod("isInWater");
            boolean inWater = (boolean) isInWaterMethod.invoke(player);

            java.lang.reflect.Method onGroundMethod = player.getClass().getMethod("onGround");
            boolean onGround = (boolean) onGroundMethod.invoke(player);

            java.lang.reflect.Method getDelta = player.getClass().getMethod("getDeltaMovement");
            Object vec3d = getDelta.invoke(player);

            if (vec3d != null) {
                double x = (double) vec3d.getClass().getField("x").get(vec3d);
                double y = (double) vec3d.getClass().getField("y").get(vec3d);
                double z = (double) vec3d.getClass().getField("z").get(vec3d);

                double multiplier = 1.0;
                if (onGround) {
                    multiplier = SpeedSettings.walkMultiplier;
                } else if (inWater) {
                    multiplier = SpeedSettings.waterMultiplier;
                }

                java.lang.reflect.Method setDelta = player.getClass().getMethod("setDeltaMovement", vec3d.getClass());
                Object newVec3d = vec3d.getClass().getConstructor(double.class, double.class, double.class)
                        .newInstance(x * multiplier, y, z * multiplier);
                setDelta.invoke(player, newVec3d);
            }
        } catch (Exception e) {
            // Резервный блок Yarn
            try {
                java.lang.reflect.Method isSubmergedInWater = player.getClass().getMethod("isSubmergedInWater");
                boolean inWaterYarn = (boolean) isSubmergedInWater.invoke(player);

                boolean onGroundYarn = false;
                try {
                    onGroundYarn = (boolean) player.getClass().getMethod("onGround").invoke(player);
                } catch (Exception ex) {
                    onGroundYarn = (boolean) player.getClass().getMethod("isOnGround").invoke(player);
                }

                java.lang.reflect.Method getVelocity = player.getClass().getMethod("getVelocity");
                Object vec3dYarn = getVelocity.invoke(player);

                if (vec3dYarn != null) {
                    double x = (double) vec3dYarn.getClass().getField("x").get(vec3dYarn);
                    double y = (double) vec3dYarn.getClass().getField("y").get(vec3dYarn);
                    double z = (double) vec3dYarn.getClass().getField("z").get(vec3dYarn);

                    double multiplier = 1.0;
                    if (onGroundYarn) {
                        multiplier = SpeedSettings.walkMultiplier;
                    } else if (inWaterYarn) {
                        multiplier = SpeedSettings.waterMultiplier;
                    }

                    java.lang.reflect.Method setVelocity = player.getClass().getMethod("setVelocity", vec3dYarn.getClass());
                    Object newVec3dYarn = vec3dYarn.getClass().getConstructor(double.class, double.class, double.class)
                            .newInstance(x * multiplier, y, z * multiplier);
                    setVelocity.invoke(player, newVec3dYarn);
                }
            } catch (Exception ignored) {}
        }
    }
}
