/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public final class ManorWeaponCatalog {
    public static final String APPEARANCE_TAG = "FTBossManorWeapon";
    public static final String DORA_APPEARANCE = "dora";
    public static final List<Entry> ENTRIES = List.of(new Entry("ftboss:statue_youzi", Form.DEFAULT), new Entry("ftboss:dandan_scythe", Form.DEFAULT), new Entry("ftboss:dora_nova_launcher", Form.DEFAULT), new Entry("ftboss:dora_pulse_gun", Form.DEFAULT), new Entry("ftboss:dora_lance", Form.DEFAULT), new Entry("ftboss:black_cleaver", Form.DEFAULT), new Entry("ftboss:lemoncat_skillet", Form.DEFAULT), new Entry("ftboss:lemoncat_spatula", Form.DEFAULT), new Entry("tridents_n_stuff:stormseeker", Form.DEFAULT), new Entry("tridents_n_stuff:the_desecrator", Form.DEFAULT), new Entry("tridents_n_stuff:echo_of_the_deep", Form.DEFAULT), new Entry("tridents_n_stuff:stellaris", Form.DEFAULT), new Entry("ftboss:wenxuanlan_flywheel", Form.GREEN), new Entry("ftboss:wenxuanlan_flywheel", Form.WHITE), new Entry("ftboss:shadow_sword", Form.DEFAULT), new Entry("ftboss:puffer_bat", Form.DEFAULT), new Entry("ftboss:maque_blessing_feather", Form.DEFAULT), new Entry("ftboss:xueli_skillet", Form.DEFAULT), new Entry("ftboss:xueli_rolling_pin", Form.DEFAULT), new Entry("ftboss:qizai_prayer_staff", Form.DEFAULT));
    public static final List<Entry> CREATIVE_ONLY = List.of(new Entry("tridents_n_stuff:sanguine_bond", Form.DEFAULT), new Entry("tridents_n_stuff:slumbering_trident", Form.DEFAULT), new Entry("tridents_n_stuff:frenzied_trident", Form.DEFAULT), new Entry("tridents_n_stuff:ancient_trident", Form.DEFAULT), new Entry("tridents_n_stuff:oceanic_trident", Form.DEFAULT), new Entry("tridents_n_stuff:holy_trident", Form.DEFAULT), new Entry("tridents_n_stuff:farshot_trident", Form.DEFAULT), new Entry("tridents_n_stuff:wooden_spear", Form.DEFAULT), new Entry("tridents_n_stuff:stone_spear", Form.DEFAULT), new Entry("tridents_n_stuff:iron_spear", Form.DEFAULT), new Entry("tridents_n_stuff:golden_spear", Form.DEFAULT), new Entry("tridents_n_stuff:diamond_spear", Form.DEFAULT), new Entry("tridents_n_stuff:netherite_spear", Form.DEFAULT));

    private ManorWeaponCatalog() {
    }

    public static ItemStack stack(Entry entry) {
        ResourceLocation identifier = new ResourceLocation(entry.item());
        if (!ForgeRegistries.ITEMS.containsKey(identifier)) {
            return ItemStack.f_41583_;
        }
        Item item = (Item)ForgeRegistries.ITEMS.getValue(identifier);
        if (item == null || item == Items.f_41852_) {
            return ItemStack.f_41583_;
        }
        ItemStack stack = new ItemStack((ItemLike)item);
        switch (entry.form()) {
            case DORA: {
                stack.m_41784_().m_128359_(APPEARANCE_TAG, DORA_APPEARANCE);
                stack.m_41714_((Component)Component.m_237115_((String)"item.ftboss.dora_nova_launcher").m_130938_(style -> style.m_131155_(Boolean.valueOf(false))));
                break;
            }
            case GREEN: {
                stack.m_41784_().m_128379_("XuanlanWhite", false);
                break;
            }
            case WHITE: {
                stack.m_41784_().m_128379_("XuanlanWhite", true);
                break;
            }
        }
        return stack;
    }

    public static void addTo(CreativeModeTab.Output output) {
        ItemStack stack;
        for (Entry entry : ENTRIES) {
            stack = ManorWeaponCatalog.stack(entry);
            if (stack.m_41619_()) continue;
            output.m_246342_(stack);
        }
        for (Entry entry : CREATIVE_ONLY) {
            stack = ManorWeaponCatalog.stack(entry);
            if (stack.m_41619_()) continue;
            output.m_246342_(stack);
        }
    }

    public record Entry(String item, Form form) {
    }

    public static enum Form {
        DEFAULT,
        DORA,
        GREEN,
        WHITE;

    }
}

