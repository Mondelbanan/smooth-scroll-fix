package name.modid.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ExampleMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        // 1. Бесконечный воздух
        player.setAirSupply(player.getMaxAirSupply());

        // 2. Разгон
        boolean isMoving = player.input.sidewaysImpulse != 0 || player.input.forwardImpulse != 0;


        if (isMoving && !player.isInLava() && !player.onClimbable()) {
            Vec3 currentVelocity = player.getDeltaMovement();

            // Множитель 1.35
            double multiplier = 1.35; 

            player.setDeltaMovement(
                currentVelocity.x * multiplier,
                currentVelocity.y, 
                currentVelocity.z * multiplier
            );
        }
    }
}
