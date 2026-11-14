/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.StatueBuildInspectorScreen;
import net.farmingtales.ftboss.client.StatueWeaponBondGuideEntry;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.farmingtales.ftboss.util.BossPreviewLayout;
import net.farmingtales.ftboss.util.BossPreviewSummaryRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;

public final class BossPreviewScreen
extends AbstractContainerScreen<BossPreviewMenu> {
    private static final ResourceLocation CATALOG = new ResourceLocation("ftboss", "boss_preview/catalog.json");
    private static final String SEALED_HEADER = "R7xQ9mK4vZ2nT8pL";
    private static final String SEALED_TEXT = "R7xQ9mK4vZ2nT8pL 6bY3hW5sD1cF0jA\nP4uG8eN2rV6xC9kM 3qJ7tB5zH1wS0dL\nX6aR2vK8mF4pT9nQ 5cZ1yG7hW3sD0jB";
    private final List<JsonObject> entries = new ArrayList<JsonObject>();
    private List<FormattedCharSequence> guide = List.of();
    private LivingEntity projection;
    private int active = -3;
    private int manorSelection;
    private float yaw = 25.0f;
    private float pitch = -8.0f;
    private float zoom = 1.0f;
    private boolean spinning;
    private boolean draggingProjection;
    private Button previous;
    private Button next;
    private Button spin;

    public BossPreviewScreen(BossPreviewMenu bossPreviewMenu, Inventory inventory, Component component) {
        super((AbstractContainerMenu)bossPreviewMenu, inventory, component);
        this.f_97726_ = 320;
        this.f_97727_ = 232;
        this.f_97730_ = 148;
        this.f_97731_ = 134;
    }

    protected void m_7856_() {
        this.f_97726_ = BossPreviewLayout.modelWidth(this.f_96543_);
        super.m_7856_();
        this.entries.clear();
        try (InputStreamReader inputStreamReader = new InputStreamReader(this.f_96541_.m_91098_().m_215595_(CATALOG), StandardCharsets.UTF_8);){
            JsonArray jsonArray = JsonParser.parseReader((Reader)inputStreamReader).getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                this.entries.add(jsonElement.getAsJsonObject());
            }
        }
        catch (Exception exception) {
            FTBossMod.LOGGER.error("[FTBoss][BossPreview] Cannot load catalogue", (Throwable)exception);
        }
        this.previous = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"<"), button -> this.selectManor(-1)).m_252987_(this.f_97735_ + BossPreviewLayout.previousX(this.f_97726_), this.f_97736_ + 114, 20, 18).m_253136_());
        this.next = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)">"), button -> this.selectManor(1)).m_252987_(this.f_97735_ + BossPreviewLayout.nextX(this.f_97726_), this.f_97736_ + 114, 20, 18).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)"ui.ftboss.preview.reset"), button -> this.resetView()).m_252987_(this.f_97735_ + 9, this.f_97736_ + 114, 48, 18).m_253136_());
        this.spin = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)"ui.ftboss.preview.rotate"), button -> {
            this.spinning = !this.spinning;
        }).m_252987_(this.f_97735_ + 61, this.f_97736_ + 114, 56, 18).m_253136_());
        this.active = -3;
        this.refreshProjection();
        this.addBuildInspectorEntry();
        this.m_142416_((GuiEventListener)StatueWeaponBondGuideEntry.create((Screen)this));
    }

    private void addBuildInspectorEntry() {
        int n = 132;
        int n2 = 16;
        int n3 = this.f_97736_ + this.f_97727_ - 21;
        int n4 = this.f_97736_ + this.f_97727_ + 6;
        int n5 = this.f_97736_ - n2 - 6;
        int n6 = n4 + n2 <= this.f_96544_ ? n4 : (n5 >= 0 ? n5 : n3);
        this.m_142416_((GuiEventListener)StatueBuildInspectorScreen.previewEntry(((BossPreviewMenu)this.f_97732_).statuePos(), this::inspectedWeapon, this::canInspectBuild, this.f_97735_ + 8, n6, n));
    }

    private ItemStack inspectedWeapon() {
        if (this.f_96541_ == null || this.f_96541_.f_91074_ == null) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack = this.f_96541_.f_91074_.m_21205_();
        if (FourWeaponFoundationAdapter.applies(itemStack) || SwordCavityBuild.applies(itemStack) || FoodWeaponEvolution.isEvolutionCandidate(itemStack)) {
            return itemStack;
        }
        ItemStack itemStack2 = this.f_96541_.f_91074_.m_21206_();
        return FourWeaponFoundationAdapter.applies(itemStack2) || SwordCavityBuild.applies(itemStack2) || FoodWeaponEvolution.isEvolutionCandidate(itemStack2) ? itemStack2 : ItemStack.f_41583_;
    }

    private boolean canInspectBuild() {
        return this.f_96541_ != null && this.f_96541_.f_91074_ != null && this.f_96541_.f_91074_.f_36096_ == this.f_97732_ && ((BossPreviewMenu)this.f_97732_).m_142621_().m_41619_();
    }

    private void updateModelControls() {
        this.next.f_93624_ = this.previous.f_93624_ = ((BossPreviewMenu)this.f_97732_).profile() == 10;
    }

    private void resetView() {
        this.yaw = 25.0f;
        this.pitch = -8.0f;
        this.zoom = 1.0f;
        this.spinning = false;
    }

    private void selectManor(int n) {
        this.manorSelection = Math.floorMod(this.manorSelection + n, Math.max(1, this.entries.size() - 10));
        this.refreshProjection();
    }

    private void refreshProjection() {
        boolean bl = ((BossPreviewMenu)this.f_97732_).profile() == 10;
        int n = bl ? 10 + this.manorSelection : ((BossPreviewMenu)this.f_97732_).profile();
        boolean bl2 = this.spin.f_93623_ = n >= 0;
        if (n == this.active) {
            this.updateModelControls();
            return;
        }
        this.active = n;
        this.projection = null;
        this.guide = List.of();
        this.draggingProjection = false;
        this.m_7897_(false);
        this.resetView();
        if (this.active < 0 || this.active >= this.entries.size()) {
            Component component = this.active == -2 ? BossPreviewScreen.encryptedText(SEALED_TEXT) : Component.m_237115_((String)"ui.ftboss.preview.empty");
            this.guide = this.f_96547_.m_92923_((FormattedText)component, BossPreviewLayout.unscaledWidth(this.f_97726_ - 24));
            this.updateModelControls();
            return;
        }
        JsonObject jsonObject = this.entries.get(this.active);
        MutableComponent mutableComponent = jsonObject.has("summary") ? Component.m_237113_((String)jsonObject.get("summary").getAsString()) : Component.m_237115_((String)"ui.ftboss.preview.no_summary");
        this.guide = this.f_96547_.m_92923_((FormattedText)mutableComponent, BossPreviewLayout.unscaledWidth(BossPreviewLayout.summaryWidth(this.f_97726_)));
        this.updateModelControls();
        try {
            Object object;
            EntityType entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(jsonObject.get("entity").getAsString()));
            if (entityType != null && (object = entityType.m_20615_((Level)this.f_96541_.f_91073_)) instanceof LivingEntity) {
                LivingEntity livingEntity;
                this.projection = livingEntity = (LivingEntity)object;
                if (livingEntity instanceof EasterBossEntity) {
                    object = (EasterBossEntity)livingEntity;
                    ((EasterBossEntity)object).setVariant(jsonObject.get("variant").getAsInt());
                }
                if (jsonObject.has("weapon") && (object = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(jsonObject.get("weapon").getAsString()))) != null) {
                    livingEntity.m_8061_(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)object));
                }
                if (livingEntity instanceof Mob) {
                    object = (Mob)livingEntity;
                    object.m_21557_(true);
                }
                livingEntity.m_20225_(true);
                livingEntity.m_20340_(false);
            }
        }
        catch (Exception exception) {
            FTBossMod.LOGGER.error("[FTBoss][BossPreview] Cannot create local projection {}", (Object)this.active, (Object)exception);
        }
    }

    protected void m_181908_() {
        super.m_181908_();
        this.refreshProjection();
        if (this.spinning) {
            this.yaw = Mth.m_14177_((float)(this.yaw + 1.5f));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void m_7286_(GuiGraphics guiGraphics, float f, int n, int n2) {
        this.refreshProjection();
        guiGraphics.m_280509_(this.f_97735_, this.f_97736_, this.f_97735_ + this.f_97726_, this.f_97736_ + this.f_97727_, -266853330);
        guiGraphics.m_280509_(this.f_97735_ + 5, this.f_97736_ + 26, this.f_97735_ + this.f_97726_ - 5, this.f_97736_ + 112, -15985377);
        guiGraphics.m_280509_(this.f_97735_ + 6, this.f_97736_ + 23, this.f_97735_ + this.f_97726_ - 6, this.f_97736_ + 24, -9249304);
        for (Slot slot : ((BossPreviewMenu)this.f_97732_).f_38839_) {
            guiGraphics.m_280509_(this.f_97735_ + slot.f_40220_ - 1, this.f_97736_ + slot.f_40221_ - 1, this.f_97735_ + slot.f_40220_ + 17, this.f_97736_ + slot.f_40221_ + 17, -11177864);
            guiGraphics.m_280509_(this.f_97735_ + slot.f_40220_, this.f_97736_ + slot.f_40221_, this.f_97735_ + slot.f_40220_ + 16, this.f_97736_ + slot.f_40221_ + 16, -14272959);
        }
        if (this.projection != null) {
            float f2 = Math.max(this.projection.m_20206_(), this.projection.m_20205_() * 1.4f);
            int n3 = Math.max(1, Math.round(65.0f / Math.max(1.8f, f2) * this.zoom));
            guiGraphics.m_280588_(this.f_97735_ + 6, this.f_97736_ + 27, this.f_97735_ + BossPreviewLayout.projectionRight(this.f_97726_), this.f_97736_ + 111);
            try {
                this.projection.f_20883_ = 180.0f;
                this.projection.m_146922_(180.0f);
                this.projection.f_20885_ = 180.0f;
                this.projection.f_20886_ = 180.0f;
                Quaternionf quaternionf = new Quaternionf().rotationZ((float)Math.PI).rotateX(this.pitch * ((float)Math.PI / 180)).rotateY(this.yaw * ((float)Math.PI / 180));
                InventoryScreen.m_280432_((GuiGraphics)guiGraphics, (int)(this.f_97735_ + (6 + BossPreviewLayout.projectionRight(this.f_97726_)) / 2), (int)(this.f_97736_ + 101), (int)n3, (Quaternionf)quaternionf, null, (LivingEntity)this.projection);
            }
            finally {
                guiGraphics.m_280618_();
            }
        }
        if (this.active < 0) {
            for (int i = 0; i < Math.min(this.guide.size(), 6); ++i) {
                this.drawSmallText(guiGraphics, this.guide.get(i), this.f_97735_ + 12, this.f_97736_ + 38 + i * 11, 13886954);
            }
        }
        if (this.active >= 0 && this.active < this.entries.size()) {
            int n4 = this.f_97735_ + BossPreviewLayout.summaryX(this.f_97726_);
            guiGraphics.m_280509_(n4 - 7, this.f_97736_ + 32, n4 - 6, this.f_97736_ + 106, -13217696);
            this.drawSmallText(guiGraphics, Component.m_237115_((String)"ui.ftboss.preview.role").m_7532_(), n4, this.f_97736_ + 33, 8905963);
            for (int i = 0; i < this.guide.size(); ++i) {
                this.drawSmallText(guiGraphics, this.guide.get(i), n4, this.f_97736_ + 48 + i * 11, 13886954);
            }
        }
        this.drawSmallText(guiGraphics, Component.m_237115_((String)"ui.ftboss.preview.input").m_7532_(), this.f_97735_ + 9, this.f_97736_ + 141, 8905963);
        List list = this.f_96547_.m_92923_((FormattedText)Component.m_237115_((String)"ui.ftboss.preview.help"), BossPreviewLayout.unscaledWidth(132));
        for (int i = 0; i < list.size(); ++i) {
            this.drawSmallText(guiGraphics, (FormattedCharSequence)list.get(i), this.f_97735_ + 9, this.f_97736_ + 179 + i * 11, 11124936);
        }
        if (((BossPreviewMenu)this.f_97732_).profile() == 10) {
            this.drawSmallCentered(guiGraphics, this.manorSelection + 1 + " / " + (this.entries.size() - 10), this.f_97735_ + BossPreviewLayout.pageCenterX(this.f_97726_), this.f_97736_ + 119, 44, 8905963);
        }
    }

    protected void m_280003_(GuiGraphics guiGraphics, int n, int n2) {
        String string = this.active >= 0 && this.active < this.entries.size() ? this.entries.get(this.active).get("name").getAsString() : this.f_96539_.getString();
        boolean bl = BossPreviewSummaryRules.showsKills(this.active);
        Component component = this.active == -2 ? BossPreviewScreen.encryptedText(SEALED_HEADER) : Component.m_237113_((String)this.f_96547_.m_92834_(string, BossPreviewLayout.unscaledWidth(this.f_97726_ - (bl ? 150 : 24))));
        this.drawSmallText(guiGraphics, component.m_7532_(), 10, 8, 14153983);
        if (bl) {
            this.drawSmallCentered(guiGraphics, Component.m_237110_((String)"ui.ftboss.preview.kills", (Object[])new Object[]{((BossPreviewMenu)this.f_97732_).playerKills()}).getString(), this.f_97726_ - 72, 8, 130, 8905963);
        }
        this.drawSmallText(guiGraphics, this.f_169604_.m_7532_(), this.f_97730_, this.f_97731_, 11124936);
    }

    private boolean overProjection(double d, double d2) {
        return d >= (double)(this.f_97735_ + 6) && d < (double)(this.f_97735_ + BossPreviewLayout.projectionRight(this.f_97726_)) && d2 >= (double)(this.f_97736_ + 27) && d2 < (double)(this.f_97736_ + 112);
    }

    public boolean m_6375_(double d, double d2, int n) {
        if (n == 0 && this.overProjection(d, d2) && ((BossPreviewMenu)this.f_97732_).m_142621_().m_41619_()) {
            this.draggingProjection = true;
            this.m_7897_(true);
            return true;
        }
        return super.m_6375_(d, d2, n);
    }

    public boolean m_7979_(double d, double d2, int n, double d3, double d4) {
        if (n == 0 && this.draggingProjection) {
            this.spinning = false;
            this.yaw = Mth.m_14177_((float)(this.yaw + (float)d3 * 2.0f));
            this.pitch = Mth.m_14036_((float)(this.pitch + (float)d4), (float)-50.0f, (float)50.0f);
            return true;
        }
        return super.m_7979_(d, d2, n, d3, d4);
    }

    public boolean m_6348_(double d, double d2, int n) {
        this.draggingProjection = false;
        return super.m_6348_(d, d2, n);
    }

    public boolean m_6050_(double d, double d2, double d3) {
        if (this.overProjection(d, d2)) {
            this.zoom = Mth.m_14036_((float)(this.zoom + (float)d3 * 0.1f), (float)0.6f, (float)1.8f);
            return true;
        }
        return super.m_6050_(d, d2, d3);
    }

    private static Component encryptedText(String string) {
        return Component.m_237113_((String)string).m_130940_(ChatFormatting.OBFUSCATED);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawSmallText(GuiGraphics guiGraphics, FormattedCharSequence formattedCharSequence, int n, int n2, int n3) {
        guiGraphics.m_280168_().m_85836_();
        try {
            guiGraphics.m_280168_().m_252880_((float)n, (float)n2, 0.0f);
            guiGraphics.m_280168_().m_85841_(0.9f, 0.9f, 1.0f);
            guiGraphics.m_280649_(this.f_96547_, formattedCharSequence, 0, 0, n3, false);
        }
        finally {
            guiGraphics.m_280168_().m_85849_();
        }
    }

    private void drawSmallCentered(GuiGraphics guiGraphics, String string, int n, int n2, int n3, int n4) {
        String string2 = this.f_96547_.m_92834_(string, BossPreviewLayout.unscaledWidth(n3));
        int n5 = (int)Math.ceil((float)this.f_96547_.m_92895_(string2) * 0.9f);
        this.drawSmallText(guiGraphics, Component.m_237113_((String)string2).m_7532_(), n - n5 / 2, n2, n4);
    }

    public void m_88315_(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.m_280273_(guiGraphics);
        this.refreshProjection();
        super.m_88315_(guiGraphics, n, n2, f);
        this.m_280072_(guiGraphics, n, n2);
    }
}

