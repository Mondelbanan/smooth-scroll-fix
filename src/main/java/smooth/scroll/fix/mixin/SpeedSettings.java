package name.modid.mixin;

public class SpeedSettings {
    public static double walkMultiplier = 1.15;
    public static double waterMultiplier = 1.08;
    private static boolean registered = false;

    public static void tryRegister() {
        if (registered) return;
        registered = true;
        try {
            Class<?> callbackClass = Class.forName("net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback");
            Object eventInstance = callbackClass.getField("EVENT").get(null);
            
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[]{callbackClass},
                (p, method, args) -> {
                    if (method.getName().equals("register")) {
                        setup(args[0]); // args[0] — это CommandDispatcher
                    }
                    return null;
                }
            );
            callbackClass.getMethod("register", Object.class).invoke(eventInstance, proxy);
        } catch (Exception ignored) {}
    }

    private static void setup(Object dispatcher) {
        try {
            Class<?> literalBuilder = Class.forName("com.mojang.brigadier.builder.LiteralArgumentBuilder");
            Class<?> requiredBuilder = Class.forName("com.mojang.brigadier.builder.RequiredArgumentBuilder");
            Class<?> doubleType = Class.forName("com.mojang.brigadier.arguments.DoubleArgumentType");
            Class<?> commandClass = Class.forName("com.mojang.brigadier.Command");

            Object speedCommand = literalBuilder.getMethod("literal", String.class).invoke(null, "speed");
            Object walkNode = literalBuilder.getMethod("literal", String.class).invoke(null, "walk");
            Object waterNode = literalBuilder.getMethod("literal", String.class).invoke(null, "water");

            Object doubleArg = doubleType.getMethod("doubleArg", double.class, double.class).invoke(null, 0.0, 3.0);
            Object walkArg = requiredBuilder.getMethod("argument", String.class, Class.forName("com.mojang.brigadier.arguments.ArgumentType")).invoke(null, "value", doubleArg);
            Object waterArg = requiredBuilder.getMethod("argument", String.class, Class.forName("com.mojang.brigadier.arguments.ArgumentType")).invoke(null, "value", doubleArg);

            Object walkExec = java.lang.reflect.Proxy.newProxyInstance(commandClass.getClassLoader(), new Class<?>[]{commandClass}, (p, m, args) -> {
                Object ctx = args[0];
                walkMultiplier = (double) doubleType.getMethod("getDouble", Class.forName("com.mojang.brigadier.context.CommandContext"), String.class).invoke(null, ctx, "value");
                sendMessage(ctx, "§aСкорость ходьбы: " + walkMultiplier);
                return 1;
            });

            Object waterExec = java.lang.reflect.Proxy.newProxyInstance(commandClass.getClassLoader(), new Class<?>[]{commandClass}, (p, m, args) -> {
                Object ctx = args[0];
                waterMultiplier = (double) doubleType.getMethod("getDouble", Class.forName("com.mojang.brigadier.context.CommandContext"), String.class).invoke(null, ctx, "value");
                sendMessage(ctx, "§bСкорость в воде: " + waterMultiplier);
                return 1;
            });

            walkArg.getClass().getMethod("executes", commandClass).invoke(walkArg, walkExec);
            waterArg.getClass().getMethod("executes", commandClass).invoke(waterArg, waterExec);

            walkNode.getClass().getMethod("then", Class.forName("com.mojang.brigadier.builder.ArgumentBuilder")).invoke(walkNode, walkArg);
            waterNode.getClass().getMethod("then", Class.forName("com.mojang.brigadier.builder.ArgumentBuilder")).invoke(waterNode, waterArg);

            speedCommand.getClass().getMethod("then", Class.forName("com.mojang.brigadier.builder.ArgumentBuilder")).invoke(speedCommand, walkNode);
            speedCommand.getClass().getMethod("then", Class.forName("com.mojang.brigadier.builder.ArgumentBuilder")).invoke(speedCommand, waterNode);

            dispatcher.getClass().getMethod("register", literalBuilder).invoke(dispatcher, speedCommand);
        } catch (Exception ignored) {}
    }

    private static void sendMessage(Object context, String text) {
        try {
            Object source = context.getClass().getMethod("getSource").get(context);
            try {
                Class<?> compClass = Class.forName("net.minecraft.network.chat.Component");
                Object msg = compClass.getMethod("literal", String.class).invoke(null, text);
                source.getClass().getMethod("sendFeedback", compClass).invoke(source, msg);
            } catch (Exception e) {
                Class<?> textClass = Class.forName("net.minecraft.text.Text");
                Object msg = textClass.getMethod("of", String.class).invoke(null, text);
                source.getClass().getMethod("sendError", textClass).invoke(source, msg);
            }
        } catch (Exception ignored) {}
    }
}
