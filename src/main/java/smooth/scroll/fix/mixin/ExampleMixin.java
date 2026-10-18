package name.modid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.player.LocalPlayer")
public class ExampleMixin {

    public static double walkMultiplier = 1.15;
    public static double waterMultiplier = 1.08;
    private static boolean commandsRegistered = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void modifyPlayer(CallbackInfo ci) {
        Object player = (Object) this;

        if (!commandsRegistered) {
            registerSpeedCommands();
            commandsRegistered = true;
        }

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
                    multiplier = walkMultiplier;
                } else if (inWater) {
                    multiplier = waterMultiplier;
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
                        multiplier = walkMultiplier;
                    } else if (inWaterYarn) {
                        multiplier = waterMultiplier;
                    }

                    java.lang.reflect.Method setVelocity = player.getClass().getMethod("setVelocity", vec3dYarn.getClass());
                    Object newVec3dYarn = vec3dYarn.getClass().getConstructor(double.class, double.class, double.class)
                            .newInstance(x * multiplier, y, z * multiplier);
                    setVelocity.invoke(player, newVec3dYarn);
                }
            } catch (Exception ignored) {}
        }
    }

    private void registerSpeedCommands() {
        try {
            Class<?> callbackClass = Class.forName("net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback");
            java.lang.reflect.Field EVENT = callbackClass.getField("EVENT");
            Object eventInstance = EVENT.get(null);

            java.lang.reflect.Method registerMethod = eventInstance.getClass().getMethod("register", Object.class);

            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[]{Class.forName("net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback")},
                (p, method, args) -> {
                    if (method.getName().equals("register")) {
                        Object dispatcher = args[0];
                        setupCommands(dispatcher);
                    }
                    return null;
                }
            );
            registerMethod.invoke(eventInstance, proxy);
        } catch (Exception ignored) {}
    }

    private void setupCommands(Object dispatcher) {
        try {
            Class<?> literalArgumentBuilderClass = Class.forName("com.mojang.brigadier.builder.LiteralArgumentBuilder");
            Class<?> requiredArgumentBuilderClass = Class.forName("com.mojang.brigadier.builder.RequiredArgumentBuilder");
            Class<?> doubleArgumentTypeClass = Class.forName("com.mojang.brigadier.arguments.DoubleArgumentType");
            Class<?> argumentsClass = Class.forName("com.mojang.brigadier.arguments.ArgumentType");

            java.lang.reflect.Method literalMethod = literalArgumentBuilderClass.getMethod("literal", String.class);
            java.lang.reflect.Method argumentMethod = requiredArgumentBuilderClass.getMethod("argument", String.class, argumentsClass);
            java.lang.reflect.Method doubleTypeMethod = doubleArgumentTypeClass.getMethod("doubleArg", double.class, double.class);
            
            Object speedCommand = literalMethod.invoke(null, "speed");

            Object walkNode = literalMethod.invoke(null, "walk");
            Object walkValueArg = argumentMethod.invoke(null, "value", doubleTypeMethod.invoke(null, 0.0, 3.0));
            
            java.lang.reflect.Method executesMethod = walkValueArg.getClass().getMethod("executes", Class.forName("com.mojang.brigadier.Command"));
            Object walkExecutor = java.lang.reflect.Proxy.newProxyInstance(
                walkValueArg.getClass().getClassLoader(),
                new Class<?>[]{Class.forName("com.mojang.brigadier.Command")},
                (p, method, args) -> {
                    Object context = args[0];
                    java.lang.reflect.Method getDouble = doubleArgumentTypeClass.getMethod("getDouble", context.getClass(), String.class);
                    walkMultiplier = (double) getDouble.invoke(null, context, "value");
                    sendClientMessage(context, "§aСкорость ходьбы изменена на: " + walkMultiplier);
                    return 1;
                }
            );
            executesMethod.invoke(walkValueArg, walkExecutor);
            
            java.lang.reflect.Method thenMethod = walkNode.getClass().getMethod("then", Class.forName("com.mojang.brigadier.builder.ArgumentBuilder"));
            thenMethod.invoke(walkNode, walkValueArg);

            Object waterNode = literalMethod.invoke(null, "water");
            Object waterValueArg = argumentMethod.invoke(null, "value", doubleTypeMethod.invoke(null, 0.0, 3.0));
            
            Object waterExecutor = java.lang.reflect.Proxy.newProxyInstance(
                waterValueArg.getClass().getClassLoader(),
                new Class<?>[]{Class.forName("com.mojang.brigadier.Command")},
                (p, method, args) -> {
                    Object context = args[0];
                    java.lang.reflect.Method getDouble = doubleArgumentTypeClass.getMethod("getDouble", context.getClass(), String.class);
                    waterMultiplier = (double) getDouble.invoke(null, context, "value");
                    sendClientMessage(context, "§bСкорость в воде изменена на: " + waterMultiplier);
                    return 1;
                }
            );
            executesMethod.invoke(waterValueArg, waterExecutor);
            thenMethod.invoke(waterNode, waterValueArg);

            thenMethod.invoke(speedCommand, walkNode);
            thenMethod.invoke(speedCommand, waterNode);

            java.lang.reflect.Method registerInDispatcher = dispatcher.getClass().getMethod("register", literalArgumentBuilderClass);
            registerInDispatcher.invoke(dispatcher, speedCommand);

        } catch (Exception ignored) {}
    }

    private void sendClientMessage(Object context, String text) {
        try {
            java.lang.reflect.Method getSource = context.getClass().getMethod("getSource");
            Object source = getSource.invoke(context);
            java.lang.reflect.Method sendFeedback = source.getClass().getMethod("sendFeedback", Class.forName("net.minecraft.network.chat.Component"));
            
            Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component");
            java.lang.reflect.Method literalText = componentClass.getMethod("literal", String.class);
            Object messageComponent = literalText.invoke(null, text);
            
            sendFeedback.invoke(source, messageComponent);
        } catch (Exception e) {
            try {
                Object source = context.getClass().getMethod("getSource").get(context);
                java.lang.reflect.Method sendError = source.getClass().getMethod("sendError", Class.forName("net.minecraft.text.Text"));
                Class<?> textClass = Class.forName("net.minecraft.text.Text");
                Object textComponent = textClass.getMethod("of", String.class).invoke(null, text);
