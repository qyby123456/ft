package net.farmingtales.ftboss.client;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** 石像主题运行时契约检查；2026-10-24 检查冻结接口、配色与不覆盖交互事件的按钮继承。 */
public final class ThemeContractCheck {
    public static void main(String[] arguments) throws Exception {
        int[] legacy = {0xC5B99E, 0xECA8D8, 0xD35C58, 0x67C98A, 0xA879D4, 0xD8B567,
            0x65CADA, 0x8DB15F, 0xD66B71, 0x5FAED8, 0x6B86D7};
        Set<String> identities = new HashSet<>();
        for (int tint = 0; tint <= 10; tint++) {
            StatueUiTheme theme = StatueUiTheme.forTint(tint);
            require(identities.add(theme.id), "duplicate identity");
            require(StatueUiTheme.fromAccent(theme.accent) == theme, "new accent identity");
            require(StatueUiTheme.fromAccent(legacy[tint]) == theme, "legacy accent identity");
            require(StatueUiTheme.fromAccent(0x80000000 | legacy[tint]) == theme, "alpha-independent accent identity");
            require(theme.texture.getPath().equals("textures/gui/statue_themes/" + theme.id + "/crest.png"), "crest path");
            for (String name : new String[] {"background", "panel", "card", "text", "muted", "accent", "accentBright", "border"}) {
                require(theme.getClass().getField(name).getInt(theme) >>> 24 == 255, "opaque ARGB field: " + name);
            }
        }
        require(StatueUiTheme.forTint(-1) == StatueUiTheme.forTint(0), "negative tint fallback");
        require(StatueUiTheme.forTint(11) == StatueUiTheme.forTint(0), "unknown tint fallback");
        require(StatueUiTheme.fromAccent(0x123456) == StatueUiTheme.forTint(0), "unknown accent fallback");
        require(StatueUiTheme.fromScreen(null) == StatueUiTheme.forTint(0), "null parent fallback");
        require(StatueUiTheme.fromScreen(new ThemedScreen(StatueUiTheme.forTint(9))) == StatueUiTheme.forTint(9), "parent source theme");
        AtomicInteger presses = new AtomicInteger();
        Button button = StatueThemeButton.create(Component.literal("test"), control -> presses.incrementAndGet(), 10, 10, 68, 20,
                () -> StatueUiTheme.forTint(6));
        button.onPress();
        require(presses.get() == 1, "action callback");
        button.setFocused(true);
        require(button.isFocused(), "keyboard focus");
        button.active = false;
        require(!button.keyPressed(257, 0, 0), "disabled Enter");
        require(!button.mouseClicked(12, 12, 0), "disabled click");
        require(presses.get() == 1, "disabled event dispatched action");
        require(StatueThemeButton.class.getSuperclass() == Button.class, "vanilla Button inheritance");
        for (var method : StatueThemeButton.class.getDeclaredMethods()) {
            require(!Set.of("mouseClicked", "mouseReleased", "keyPressed", "onPress", "playDownSound", "setFocused", "updateWidgetNarration")
                    .contains(method.getName()), "interaction override: " + method.getName());
        }
        for (var field : StatueUiTheme.class.getDeclaredFields()) {
            require(!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()), "mutable global theme state");
        }
        System.out.println("PASS: 11 identities, 33 accent roundtrips, fallbacks, parent inheritance, ARGB, texture paths, button callback/focus/disabled events, inherited sound/narration, no mutable global theme");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class ThemedScreen extends Screen implements StatueThemeSource {
        private final StatueUiTheme theme;

        private ThemedScreen(StatueUiTheme theme) {
            super(Component.literal("theme check"));
            this.theme = theme;
        }

        @Override
        public StatueUiTheme statueTheme() {
            return this.theme;
        }
    }
}
