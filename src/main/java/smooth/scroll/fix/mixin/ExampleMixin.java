package name.modid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.player.LocalPlayer")
public class ExampleMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void modifyPlayer(CallbackInfo ci) {
        Object player = (Object) this;

        // 1. БЕСКОНЕЧНЫЙ ВОЗДУХ (Mojmap)
        try {
            java.lang.reflect.Method setAirSupply = player.getClass().getMethod("setAirSupply", int.class);
            setAirSupply.invoke(player, 300);
        } catch (Exception e) {
            // Резервный блок для Yarn
            try {
                java.lang.reflect.Method setAir = player.getClass().getMethod("setAir", int.class);
                setAir.setAccessible(true);
                setAir.invoke(player, 300);
            } catch (Exception ignored) {}
        }

        // 2. УПРАВЛЕНИЕ СКОРОСТЬЮ И ФИЗИКОЙ ВОДЫ (Блок Mojmap)
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

                // Разделяем логику: на земле ускоряем до 1.15, в воде притормаживаем до 0.85 (убирает разгон)
                double multiplier = 1.0;
                if (onGround) {
                    multiplier = 1.15; 
                } else if (inWater) {
                    multiplier = 0.85; 
                }

                java.lang.reflect.Method setDelta = player.getClass().getMethod("setDeltaMovement", vec3d.getClass());
                Object newVec3d = vec3d.getClass().getConstructor(double.class, double.class, double.class)
                        .newInstance(x * multiplier, y, z * multiplier);
                setDelta.invoke(player, newVec3d);
            }
        } catch (Exception e) {
            // 3. УПРАВЛЕНИЕ СКОРОСТЬЮ И ФИЗИКОЙ ВОДЫ (Резервный блок Yarn)
            try {
                java.lang.reflect.Method isSubmergedInWater = player.getClass().getMethod("isSubmergedInWater");
                boolean inWaterYarn = (boolean) isSubmergedInWater.invoke(player);

                boolean onGroundYarn = false;
                try {
                    java.lang.reflect.Method onGroundYarnMethod = player.getClass().getMethod("onGround");
                    onGroundYarn = (boolean) onGroundYarnMethod.invoke(player);
                } catch (Exception ex) {
                    java.lang.reflect.Method isOnGroundYarn = player.getClass().getMethod("isOnGround");
                    onGroundYarn = (boolean) isOnGroundYarn.invoke(player);
                }

                java.lang.reflect.Method getVelocity = player.getClass().getMethod("getVelocity");
                Object vec3dYarn = getVelocity.invoke(player);

                if (vec3dYarn != null) {
                    double x = (double) vec3dYarn.getClass().getField("x").get(vec3dYarn);
                    double y = (double) vec3dYarn.getClass().getField("y").get(vec3dYarn);
                    double z = (double) vec3dYarn.getClass().getField("z").get(vec3dYarn);

                    double multiplier = 1.0;
                    if (onGroundYarn) {
                        multiplier = 1.15;
                    } else if (inWaterYarn) {
                        multiplier = 0.85;
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
