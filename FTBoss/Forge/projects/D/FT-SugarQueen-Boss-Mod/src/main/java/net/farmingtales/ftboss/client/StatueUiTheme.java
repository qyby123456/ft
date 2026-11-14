package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** 石像界面不可变主题；2026-10-24 按真实 Boss 身份选择纹章与材质，白膜和金像不改变归属。 */
public final class StatueUiTheme {
    private static final StatueUiTheme[] THEMES = {
        new StatueUiTheme("neutral", 0x10151C, 0x1A222C, 0x252F3B, 0xF1EBDA, 0xB3B8BE, 0xBAA375, 0xF0DBA7, 0x655B48),
        new StatueUiTheme("sugar_queen", 0x17131F, 0x251D2C, 0x332839, 0xFFF2DF, 0xC3B3C4, 0xE9A3CB, 0xBCECF1, 0x73516E),
        new StatueUiTheme("butcher", 0x1A1213, 0x2A1D1D, 0x382A29, 0xF5E5C8, 0xC0AAA0, 0xD77366, 0xE8D3AA, 0x76504B),
        new StatueUiTheme("saya", 0x101B17, 0x1C2922, 0x29392D, 0xF5F0D6, 0xAFC2AA, 0x83BE91, 0xE9E3B9, 0x526B52),
        new StatueUiTheme("wine_barrel", 0x1D1317, 0x2C1F23, 0x3B2B2B, 0xF7E6CA, 0xC4AEA8, 0xCCAA63, 0xE9D399, 0x71523E),
        new StatueUiTheme("rat_swarm", 0x191814, 0x27261F, 0x353227, 0xF5EDCF, 0xBEB79E, 0xD9B766, 0xF6DEA0, 0x76633D),
        new StatueUiTheme("defect", 0x0F191E, 0x192A32, 0x243A43, 0xE4F3EE, 0xA6BEC2, 0x64CDCF, 0xEBC178, 0x456D77),
        new StatueUiTheme("compost", 0x191911, 0x29281B, 0x373325, 0xF0E8CB, 0xC0B69C, 0xA4BC70, 0xD0ACD9, 0x6A6642),
        new StatueUiTheme("apple", 0x1A1813, 0x2B251C, 0x3A3124, 0xF8EED4, 0xC1B59C, 0xDF7A79, 0xD9CD87, 0x775945),
        new StatueUiTheme("tide_fish", 0x101B24, 0x192B38, 0x243C49, 0xE6F1E7, 0xA8C2CA, 0x6CBFCB, 0xE8DDB5, 0x446E83),
        new StatueUiTheme("benger", 0x171C19, 0x232D27, 0x303B31, 0xF5ECD7, 0xB4C0AA, 0xA4BC91, 0xE1C39A, 0x67745A)
    };
    private static final int[] LEGACY_ACCENTS = {
        0xC5B99E, 0xECA8D8, 0xD35C58, 0x67C98A, 0xA879D4, 0xD8B567,
        0x65CADA, 0x8DB15F, 0xD66B71, 0x5FAED8, 0x6B86D7
    };

    public final String id;
    public final int background;
    public final int panel;
    public final int card;
    public final int text;
    public final int muted;
    public final int accent;
    public final int accentBright;
    public final int border;
    public final ResourceLocation texture;
    public final ResourceLocation atlasTexture;

    private StatueUiTheme(String id, int background, int panel, int card, int text, int muted, int accent, int accentBright, int border) {
        this.id = id;
        this.background = 0xFF000000 | background;
        this.panel = 0xFF000000 | panel;
        this.card = 0xFF000000 | card;
        this.text = 0xFF000000 | text;
        this.muted = 0xFF000000 | muted;
        this.accent = 0xFF000000 | accent;
        this.accentBright = 0xFF000000 | accentBright;
        this.border = 0xFF000000 | border;
        this.texture = new ResourceLocation("ftboss", "textures/gui/statue_themes/" + id + "/crest.png");
        this.atlasTexture = new ResourceLocation("ftboss", "textures/gui/statue_themes/" + id + "/panels.png");
    }

    public static StatueUiTheme forTint(int tint) {
        return THEMES[tint >= 0 && tint < THEMES.length ? tint : 0];
    }

    public static StatueUiTheme forStatue(BlockPos position) {
        Minecraft minecraft = Minecraft.getInstance();
        if (position == null || minecraft.level == null || !minecraft.level.hasChunkAt(position)) return forTint(0);
        BlockState state = minecraft.level.getBlockState(position);
        if (!(state.getBlock() instanceof CandyStatueBlock)) return forTint(0);
        if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lowerPosition = position.below();
            if (minecraft.level.hasChunkAt(lowerPosition)) {
                BlockState lower = minecraft.level.getBlockState(lowerPosition);
                if (lower.is(state.getBlock())) state = lower;
            }
        }
        CandyStatueBlock statue = (CandyStatueBlock) state.getBlock();
        return forTint(statue.ownTint());
    }

    public static StatueUiTheme fromScreen(Screen screen) {
        return screen instanceof StatueThemeSource source ? source.statueTheme() : forTint(0);
    }

    public static StatueUiTheme fromAccent(int accent) {
        int rgb = accent & 0xFFFFFF;
        for (int index = 0; index < THEMES.length; index++) {
            if ((THEMES[index].accent & 0xFFFFFF) == rgb || LEGACY_ACCENTS[index] == rgb) return THEMES[index];
        }
        return forTint(0);
    }
}
