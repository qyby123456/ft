/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.WeaponBondGuideLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public final class StatueWeaponBondGuideScreen
extends Screen {
    private static final ResourceLocation CATALOG = new ResourceLocation("ftboss", "weapon_bonds/guide.json");
    private static final String PREFIX = "guide.ftboss.bonds.";
    private static final int TEXT = -1774864;
    private static final int MUTED = -5455409;
    private static final int ACCENT = -1456769;
    private static final int GREEN = -7021133;
    private final List<JsonObject> catalog = new ArrayList<JsonObject>();
    private static final String[] WEAPONS = new String[]{"all", "bow", "crossbow", "staff", "axe", "trident", "sword"};
    private final List<Button> tabs = new ArrayList<Button>();
    private final List<Button> weaponButtons = new ArrayList<Button>();
    private String weapon = "all";
    private List<JsonObject> pages = List.of();
    private List<Line> lines = List.of();
    private List<Group> groups = List.of();
    private Button previous;
    private Button next;
    private int tab;
    private int page;
    private int scroll;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int contentHeight;

    private StatueWeaponBondGuideScreen() {
        super(StatueWeaponBondGuideScreen.text("title", new Object[0]));
    }

    private static Component text(String string, Object ... objectArray) {
        return Component.m_237110_((String)(PREFIX + string), (Object[])objectArray);
    }

    public static Button entry(BooleanSupplier booleanSupplier, int n, int n2, int n3) {
        return Button.m_253074_((Component)StatueWeaponBondGuideScreen.text("entry", new Object[0]), button -> {
            Minecraft minecraft = Minecraft.m_91087_();
            if (minecraft.f_91074_ == null || !booleanSupplier.getAsBoolean()) {
                return;
            }
            minecraft.f_91074_.m_6915_();
            minecraft.m_91152_((Screen)new StatueWeaponBondGuideScreen());
        }).m_252987_(n, n2, n3, 16).m_253136_();
    }

    protected void m_7856_() {
        int n;
        this.panelWidth = WeaponBondGuideLayout.panelWidth(this.f_96543_);
        this.panelHeight = WeaponBondGuideLayout.panelHeight(this.f_96544_);
        this.left = (this.f_96543_ - this.panelWidth) / 2;
        this.top = (this.f_96544_ - this.panelHeight) / 2;
        this.catalog.clear();
        try (InputStreamReader inputStreamReader = new InputStreamReader(this.f_96541_.m_91098_().m_215595_(CATALOG), StandardCharsets.UTF_8);){
            for (JsonElement jsonElement : JsonParser.parseReader((Reader)inputStreamReader).getAsJsonArray()) {
                this.catalog.add(jsonElement.getAsJsonObject());
            }
        }
        catch (Exception exception) {
            FTBossMod.LOGGER.error("[FTBoss][WeaponBondGuide] Cannot load illustrated guide", (Throwable)exception);
        }
        this.tabs.clear();
        int n2 = (this.panelWidth - 24) / 5;
        for (n = 0; n < 5; ++n) {
            int n3 = n;
            this.tabs.add((Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)StatueWeaponBondGuideScreen.text("tab." + n, new Object[0]), button -> {
                this.tab = n3;
                this.page = 0;
                this.scroll = 0;
                this.rebuild();
            }).m_252987_(this.left + 12 + n * n2, this.top + 36, n2 - 3, 20).m_253136_()));
        }
        this.weaponButtons.clear();
        n = (this.panelWidth - 24) / WEAPONS.length;
        for (int i = 0; i < WEAPONS.length; ++i) {
            String string = WEAPONS[i];
            this.weaponButtons.add((Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)StatueWeaponBondGuideScreen.text("weapon." + string, new Object[0]), button -> {
                this.weapon = string;
                this.page = 0;
                this.scroll = 0;
                this.rebuild();
            }).m_252987_(this.left + 12 + i * n, this.top + 60, n - 3, 20).m_253136_()));
        }
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)StatueWeaponBondGuideScreen.text("close", new Object[0]), button -> this.m_7379_()).m_252987_(this.left + 12, this.top + this.panelHeight - 29, 70, 20).m_253136_());
        this.previous = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"<"), button -> this.changePage(-1)).m_252987_(this.left + this.panelWidth - 110, this.top + this.panelHeight - 29, 22, 20).m_253136_());
        this.next = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)">"), button -> this.changePage(1)).m_252987_(this.left + this.panelWidth - 34, this.top + this.panelHeight - 29, 22, 20).m_253136_());
        this.rebuild();
    }

    private void changePage(int n) {
        this.page = WeaponBondGuideLayout.page(this.page + n, this.pages.size());
        this.scroll = 0;
        this.rebuild();
    }

    private void addLines(List<Line> list, Component component, int n) {
        for (FormattedCharSequence formattedCharSequence : this.f_96547_.m_92923_((FormattedText)component, this.panelWidth - 50)) {
            list.add(new Line(formattedCharSequence, n));
        }
        list.add(new Line(FormattedCharSequence.f_13691_, n));
    }

    private static boolean matchesWeapon(JsonObject jsonObject, String string) {
        if (string.equals("all") || !jsonObject.has("weapons")) {
            return true;
        }
        for (JsonElement jsonElement : jsonObject.getAsJsonArray("weapons")) {
            if (!string.equals(jsonElement.getAsString())) continue;
            return true;
        }
        return false;
    }

    private void rebuild() {
        int n;
        this.pages = this.catalog.stream().filter(jsonObject -> jsonObject.get("tab").getAsInt() == this.tab && StatueWeaponBondGuideScreen.matchesWeapon(jsonObject, this.weapon)).toList();
        this.page = WeaponBondGuideLayout.page(this.page, this.pages.size());
        ArrayList<Line> arrayList = new ArrayList<Line>();
        ArrayList<Group> arrayList2 = new ArrayList<Group>();
        if (this.pages.isEmpty()) {
            this.addLines(arrayList, StatueWeaponBondGuideScreen.text(this.catalog.isEmpty() ? "unavailable" : "none_for_weapon", new Object[0]), -5455409);
        } else {
            JsonObject jsonObject2 = this.pages.get(this.page);
            String string = jsonObject2.get("id").getAsString();
            for (JsonElement jsonElement : jsonObject2.getAsJsonArray("groups")) {
                JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                ArrayList<Icon> arrayList3 = new ArrayList<Icon>();
                for (JsonElement jsonElement2 : jsonObject3.getAsJsonArray("items")) {
                    String string2 = jsonElement2.getAsString();
                    ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string2);
                    Item item = resourceLocation == null ? null : (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
                    arrayList3.add(new Icon(new ItemStack((ItemLike)(item == null || item == Items.f_41852_ ? Items.f_42127_ : item)), string2));
                }
                arrayList2.add(new Group(List.copyOf(arrayList3), StatueWeaponBondGuideScreen.text("group." + jsonObject3.get("label").getAsString(), new Object[0]), jsonObject3.get("count").getAsInt()));
            }
            this.addLines(arrayList, StatueWeaponBondGuideScreen.text("requirement", StatueWeaponBondGuideScreen.text(string + ".requirement", new Object[0])), -1774864);
            this.addLines(arrayList, StatueWeaponBondGuideScreen.text("effect", StatueWeaponBondGuideScreen.text(string + ".effect", new Object[0])), -7021133);
            this.addLines(arrayList, StatueWeaponBondGuideScreen.text(string + ".note", new Object[0]), -5455409);
            this.addLines(arrayList, StatueWeaponBondGuideScreen.text("tab_note." + this.tab, new Object[0]), -1456769);
        }
        this.groups = List.copyOf(arrayList2);
        this.lines = List.copyOf(arrayList);
        this.contentHeight = 64 + this.lines.size() * 12;
        this.scroll = WeaponBondGuideLayout.scroll(this.scroll, this.contentHeight, this.viewportHeight());
        for (n = 0; n < this.tabs.size(); ++n) {
            this.tabs.get((int)n).f_93623_ = n != this.tab;
        }
        for (n = 0; n < this.weaponButtons.size(); ++n) {
            this.weaponButtons.get((int)n).f_93623_ = !WEAPONS[n].equals(this.weapon);
        }
        this.previous.f_93623_ = this.page > 0;
        this.next.f_93623_ = this.page + 1 < this.pages.size();
    }

    private int viewportHeight() {
        return WeaponBondGuideLayout.viewportHeight(this.panelHeight);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Icon renderDiagram(GuiGraphics guiGraphics, int n, int n2, int n3) {
        Icon icon = null;
        int n4 = n3 + 3 - this.scroll;
        int n5 = WeaponBondGuideLayout.groupWidth(this.panelWidth - 48, this.groups.size());
        for (int i = 0; i < this.groups.size(); ++i) {
            Group group = this.groups.get(i);
            int n6 = this.left + 24 + i * (n5 + 16);
            guiGraphics.m_280509_(n6, n4, n6 + n5, n4 + 53, -14338234);
            int n7 = group.icons().size() * 24 + Math.max(0, group.icons().size() - 1) * 12;
            int n8 = n6 + (n5 - n7) / 2;
            for (int j = 0; j < group.icons().size(); ++j) {
                Icon icon2 = group.icons().get(j);
                int n9 = n8 + j * 36;
                guiGraphics.m_280168_().m_85836_();
                try {
                    guiGraphics.m_280168_().m_252880_((float)n9, (float)(n4 + 3), 0.0f);
                    guiGraphics.m_280168_().m_85841_(1.5f, 1.5f, 1.0f);
                    guiGraphics.m_280480_(icon2.stack(), 0, 0);
                }
                finally {
                    guiGraphics.m_280168_().m_85849_();
                }
                if (j > 0) {
                    guiGraphics.m_280056_(this.f_96547_, "/", n9 - 9, n4 + 12, -5455409, false);
                }
                if (n2 < n3 || n2 >= n3 + this.viewportHeight() || n < n9 || n >= n9 + 24 || n2 < n4 + 3 || n2 >= n4 + 27) continue;
                icon = icon2;
            }
            String string = this.f_96547_.m_92834_(StatueWeaponBondGuideScreen.text("group_count", group.label(), group.count()).getString(), n5 - 6);
            guiGraphics.m_280056_(this.f_96547_, string, n6 + (n5 - this.f_96547_.m_92895_(string)) / 2, n4 + 37, -1774864, false);
            if (i <= 0) continue;
            guiGraphics.m_280056_(this.f_96547_, "+", n6 - 12, n4 + 20, -1456769, false);
        }
        return icon;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void m_88315_(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        int n4;
        Icon icon;
        this.m_280273_(guiGraphics);
        guiGraphics.m_280509_(this.left - 1, this.top - 1, this.left + this.panelWidth + 1, this.top + this.panelHeight + 1, -1456769);
        guiGraphics.m_280509_(this.left, this.top, this.left + this.panelWidth, this.top + this.panelHeight, -15457489);
        guiGraphics.m_280614_(this.f_96547_, this.f_96539_, this.left + 12, this.top + 10, -1456769, false);
        guiGraphics.m_280056_(this.f_96547_, this.f_96547_.m_92834_(StatueWeaponBondGuideScreen.text("subtitle", new Object[0]).getString(), this.panelWidth - 24), this.left + 12, this.top + 23, -5455409, false);
        if (!this.pages.isEmpty()) {
            guiGraphics.m_280614_(this.f_96547_, StatueWeaponBondGuideScreen.text(this.pages.get(this.page).get("id").getAsString() + ".title", new Object[0]), this.left + 18, this.top + 89, -1456769, false);
        }
        int n5 = this.top + 106;
        guiGraphics.m_280509_(this.left + 12, n5 - 2, this.left + this.panelWidth - 12, n5 + this.viewportHeight(), -15853277);
        guiGraphics.m_280588_(this.left + 13, n5, this.left + this.panelWidth - 13, n5 + this.viewportHeight());
        try {
            icon = this.renderDiagram(guiGraphics, n, n2, n5);
            for (n4 = 0; n4 < this.lines.size(); ++n4) {
                n3 = n5 + 64 + n4 * 12 - this.scroll;
                if (n3 + 12 <= n5 || n3 >= n5 + this.viewportHeight()) continue;
                Line line = this.lines.get(n4);
                guiGraphics.m_280649_(this.f_96547_, line.text(), this.left + 24, n3, line.color(), false);
            }
        }
        finally {
            guiGraphics.m_280618_();
        }
        n4 = Math.max(0, this.contentHeight - this.viewportHeight());
        if (n4 > 0) {
            n3 = Math.max(12, this.viewportHeight() * this.viewportHeight() / this.contentHeight);
            int n6 = n5 + this.scroll * (this.viewportHeight() - n3) / n4;
            guiGraphics.m_280509_(this.left + this.panelWidth - 16, n6, this.left + this.panelWidth - 13, n6 + n3, -1456769);
        }
        guiGraphics.m_280056_(this.f_96547_, this.page + 1 + "/" + Math.max(1, this.pages.size()), this.left + this.panelWidth - 82, this.top + this.panelHeight - 23, -1774864, false);
        super.m_88315_(guiGraphics, n, n2, f);
        if (icon != null) {
            guiGraphics.m_280677_(this.f_96547_, List.of(icon.stack().m_41786_(), Component.m_237113_((String)icon.id()), StatueWeaponBondGuideScreen.text("example", new Object[0])), Optional.empty(), n, n2);
        }
    }

    public boolean m_6050_(double d, double d2, double d3) {
        if (d >= (double)(this.left + 12) && d < (double)(this.left + this.panelWidth - 12) && d2 >= (double)(this.top + 106) && d2 < (double)(this.top + 106 + this.viewportHeight())) {
            this.scroll = WeaponBondGuideLayout.scroll(this.scroll - (int)Math.round(d3 * 24.0), this.contentHeight, this.viewportHeight());
            return true;
        }
        return super.m_6050_(d, d2, d3);
    }

    public boolean m_7933_(int n, int n2, int n3) {
        if (n == 267 || n == 262) {
            this.changePage(1);
        } else if (n == 266 || n == 263) {
            this.changePage(-1);
        } else if (n == 264 || n == 265) {
            this.scroll = WeaponBondGuideLayout.scroll(this.scroll + (n == 264 ? 24 : -24), this.contentHeight, this.viewportHeight());
        } else if (n == 268) {
            this.scroll = 0;
        } else if (n == 269) {
            this.scroll = WeaponBondGuideLayout.scroll(this.contentHeight, this.contentHeight, this.viewportHeight());
        } else {
            return super.m_7933_(n, n2, n3);
        }
        return true;
    }

    public void m_86600_() {
        if (this.f_96541_.f_91074_ == null || !this.f_96541_.f_91074_.m_6084_()) {
            this.m_7379_();
        }
    }

    public boolean m_7043_() {
        return false;
    }

    private record Line(FormattedCharSequence text, int color) {
    }

    private record Icon(ItemStack stack, String id) {
    }

    private record Group(List<Icon> icons, Component label, int count) {
    }
}

