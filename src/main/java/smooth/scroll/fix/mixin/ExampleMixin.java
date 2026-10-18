package smooth.scroll.fix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smooth.scroll.fix.SpeedSettings;

@Mixin(targets = "net.minecraft.client.player.LocalPlayer")
public class ExampleMixin {

    // 1. ПРИМЕНЕНИЕ СКОРОСТИ В ДВИЖЕНИИ
    @Inject(method = "tick", at = @At("HEAD"))
    private void modifyPlayer(CallbackInfo ci) {
        Object player = (Object) this;
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

    // 2. ХЕНДЛЕР ТЕКСТОВЫХ КОМАНД ЧЕРЕЗ ЧАТ
    @Inject(method = "chat", at = @At("HEAD"), cancellable = true)
    private void interceptChat(String message, CallbackInfo ci) {
        if (message.startsWith(".walk ") || message.startsWith(".water ")) {
            ci.cancel(); // Отменяем отправку сообщения на сервер, чтобы никто не увидел
            
            try {
                String[] parts = message.split(" ");
                if (parts.length < 2) return;
                double value = Double.parseDouble(parts[1]);

                if (message.startsWith(".walk ")) {
                    SpeedSettings.walkMultiplier = value;
                    SpeedSettings.sendMessage(this, "§a[Mod] Скорость ходьбы изменена на: " + value);
                } else {
                    SpeedSettings.waterMultiplier = value;
                    SpeedSettings.sendMessage(this, "§b[Mod] Скорость в воде изменена на: " + value);
                }
            } catch (Exception e) {
                SpeedSettings.sendMessage(this, "§c[Mod] Ошибка! Используйте: .walk 1.15 или .water 1.08");
            }
        }
    }
}
