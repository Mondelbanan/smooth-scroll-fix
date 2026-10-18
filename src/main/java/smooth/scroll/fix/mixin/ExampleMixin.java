package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ExampleMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // 1. БЕСКОНЕЧНЫЙ ВОЗДУХ ДЛЯ АКУЛЫ
        player.setAir(player.getMaxAir());

        // 2. СВЕРХЗВУКОВОЙ РАЗГОН (Безопасная скорость 1.35)
        // Метод hasMovementInput() автоматически проверяет нажатие W, A, S, D
        if (player.input.hasMovementInput() && !player.isInLava() && !player.isClimbing()) {
            Vec3d currentVelocity = player.getVelocity();
            double multiplier = 1.35;

            player.setVelocity(
                currentVelocity.x * multiplier,
                currentVelocity.y,
                currentVelocity.z * multiplier
            );
        }
    }
}
