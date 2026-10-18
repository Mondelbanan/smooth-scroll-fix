package smooth.scroll.fix;

public class SpeedSettings {
    public static double walkMultiplier = 1.15;
    public static double waterMultiplier = 1.08;

    public static void sendMessage(Object player, String text) {
        try {
            Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component");
            Object msg = componentClass.getMethod("literal", String.class).invoke(null, text);
            player.getClass().getMethod("sendSystemMessage", componentClass).invoke(player, msg);
        } catch (Exception e) {
            try {
                Class<?> textClass = Class.forName("net.minecraft.text.Text");
                Object msg = textClass.getMethod("of", String.class).invoke(null, text);
                player.getClass().getMethod("sendMessage", textClass, boolean.class).invoke(player, msg, false);
            } catch (Exception ignored) {}
        }
    }
}
