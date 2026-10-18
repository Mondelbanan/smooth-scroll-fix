package name.modid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.player.LocalPlayer")
public class ExampleMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Object player = (Object) this;
        try {
            // 1. БЕСКОНЕЧНЫЙ ВОЗДУХ (Используем чистый поиск метода в Java)
            java.lang.reflect.Method setAir = player.getClass().getMethod("setAirSupply", int.class);
            setAir.invoke(player, 300);

            // 2. СВЕРХЗВУКОВОЙ РАЗГОН (Умножаем вектор скорости напрямую)
            java.lang.reflect.Method getDelta = player.getClass().getMethod("getDeltaMovement");
            Object vec3d = getDelta.invoke(player);

            if (vec3d != null) {
                double x = (double) vec3d.getClass().getField("x").get(vec3d);
                double y = (double) vec3d.getClass().getField("y").get(vec3d);
                double z = (double) vec3d.getClass().getField("z").get(vec3d);

                // Коэффициент скорости 1.35
                double multiplier = 1.35;

                java.lang.reflect.Method setDelta = player.getClass().getMethod("setDeltaMovement", vec3d.getClass());
                Object newVec3d = vec3d.getClass().getConstructor(double.class, double.class, double.class).newInstance(x * multiplier, y, z * multiplier);
                setDelta.invoke(player, newVec3d);
            }
        } catch (Exception e) {
            // Если Mojang-маппинги не подошли, пробуем Yarn-названия (для ClientPlayerEntity)
            try {
                java.lang.reflect.Method setAirYarn = player.getClass().getMethod("setAir", int.class);
                setAirYarn.invoke(player, 300);

                java.lang.reflect.Method getVelocity = player.getClass().getMethod("getVelocity");
                Object vec3dYarn = getVelocity.invoke(player);

                if (vec3dYarn != null) {
                    double x = (double) vec3dYarn.getClass().getField("x").get(vec3dYarn);
                    double y = (double) vec3dYarn.getClass().getField("y").get(vec3dYarn);
                    double z = (double) vec3dYarn.getClass().getField("z").get(vec3dYarn);

                    java.lang.reflect.Method setVelocity = player.getClass().getMethod("setVelocity", vec3dYarn.getClass());
                    Object newVec3dYarn = vec3dYarn.getClass().getConstructor(double.class, double.class, double.class).newInstance(x * 1.35, y, z * 1.35);
                    setVelocity.invoke(player, newVec3dYarn);
                }
            } catch (Exception ignored) {}
        }
    }
}

