package net.farmingtales.ftboss.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** 庄园武器创造栏目录；2026-09-17，保留真实物品与技能，缺少外部模组时跳过，不复制 Boss 附魔。 */
public final class ManorWeaponCatalog {
    public static final String APPEARANCE_TAG = "FTBossManorWeapon";
    public static final String DORA_APPEARANCE = "dora";

    public enum Form { DEFAULT, DORA, GREEN, WHITE }

    public record Entry(String item, Form form) {}

    public static final List<Entry> ENTRIES = List.of(
            new Entry("ftboss:statue_youzi", Form.DEFAULT),
            new Entry("ftboss:dandan_scythe", Form.DEFAULT),
            new Entry("ftboss:dora_nova_launcher", Form.DEFAULT),
            new Entry("ftboss:dora_pulse_gun", Form.DEFAULT),
            new Entry("ftboss:dora_lance", Form.DEFAULT),
            new Entry("ftboss:black_cleaver", Form.DEFAULT),
            new Entry("ftboss:lemoncat_skillet", Form.DEFAULT),
            new Entry("ftboss:lemoncat_spatula", Form.DEFAULT),
            new Entry("tridents_n_stuff:stormseeker", Form.DEFAULT),
            new Entry("tridents_n_stuff:the_desecrator", Form.DEFAULT),
            new Entry("tridents_n_stuff:echo_of_the_deep", Form.DEFAULT),
            new Entry("tridents_n_stuff:stellaris", Form.DEFAULT),
            new Entry("ftboss:wenxuanlan_flywheel", Form.GREEN),
            new Entry("ftboss:wenxuanlan_flywheel", Form.WHITE),
            new Entry("ftboss:shadow_sword", Form.DEFAULT),
            new Entry("ftboss:puffer_bat", Form.DEFAULT),
            new Entry("ftboss:maque_blessing_feather", Form.DEFAULT),
            new Entry("ftboss:xueli_skillet", Form.DEFAULT),
            new Entry("ftboss:xueli_rolling_pin", Form.DEFAULT),
            new Entry("ftboss:qizai_prayer_staff", Form.DEFAULT)
    );

    /**
     * ★ 2026-09-24 按用户要求新增：**只进创造栏、不进战利品箱**的附赠条目。
     *
     * <p>用户原话「还是加入回去…血腥契约，其他三叉戟和长矛」，追问后确认「只放回创造栏，不进宝箱」。
     * 这些是内嵌 {@code tridents_n_stuff} 里原本拿不到（只能 {@code /give}）的东西：
     * 血腥契约 1 件 + 其余 6 把三叉戟 + 6 把长矛。
     *
     * <p><b>为什么不直接写进 {@link #ENTRIES}</b>：{@code ManorVictoryLoot.contents()} 直接遍历
     * {@code ENTRIES} 生成庄园凯旋宝箱，写进去就会连带进宝箱（用户明确不要）。所以单独一份，
     * 只由 {@link #addTo} 输出到创造栏。
     *
     * <p>它们**不是庄园武器**：不进 {@code ManorWeaponEnhancements.WEAPONS}，
     * 因此不吃庄园武器的攻击加成/特效/视检特例，行为与 TnS 原版一致。
     */
    public static final List<Entry> CREATIVE_ONLY = List.of(
            new Entry("tridents_n_stuff:sanguine_bond", Form.DEFAULT),      // 血腥契约
            new Entry("tridents_n_stuff:slumbering_trident", Form.DEFAULT), // 沉睡三叉戟
            new Entry("tridents_n_stuff:frenzied_trident", Form.DEFAULT),   // 狂暴三叉戟
            new Entry("tridents_n_stuff:ancient_trident", Form.DEFAULT),    // 远古三叉戟
            new Entry("tridents_n_stuff:oceanic_trident", Form.DEFAULT),    // 海洋三叉戟
            new Entry("tridents_n_stuff:holy_trident", Form.DEFAULT),       // 神圣三叉戟
            new Entry("tridents_n_stuff:farshot_trident", Form.DEFAULT),    // 远射三叉戟
            new Entry("tridents_n_stuff:wooden_spear", Form.DEFAULT),       // 木矛
            new Entry("tridents_n_stuff:stone_spear", Form.DEFAULT),        // 石矛
            new Entry("tridents_n_stuff:iron_spear", Form.DEFAULT),         // 铁矛
            new Entry("tridents_n_stuff:golden_spear", Form.DEFAULT),       // 金矛
            new Entry("tridents_n_stuff:diamond_spear", Form.DEFAULT),      // 钻石矛
            new Entry("tridents_n_stuff:netherite_spear", Form.DEFAULT)     // 下界合金矛
    );

    private ManorWeaponCatalog() {}

    public static ItemStack stack(Entry entry) {
        ResourceLocation identifier = new ResourceLocation(entry.item());
        if (!ForgeRegistries.ITEMS.containsKey(identifier)) return ItemStack.EMPTY;
        var item = ForgeRegistries.ITEMS.getValue(identifier);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        switch (entry.form()) {
            case DORA -> {
                stack.getOrCreateTag().putString(APPEARANCE_TAG, DORA_APPEARANCE);
                stack.setHoverName(Component.translatable("item.ftboss.dora_nova_launcher").withStyle(style -> style.withItalic(false)));
            }
            case GREEN -> stack.getOrCreateTag().putBoolean("XuanlanWhite", false);
            case WHITE -> stack.getOrCreateTag().putBoolean("XuanlanWhite", true);
            default -> {}
        }
        return stack;
    }

    public static void addTo(CreativeModeTab.Output output) {
        for (Entry entry : ENTRIES) {
            ItemStack stack = stack(entry);
            if (!stack.isEmpty()) output.accept(stack);
        }
        /* ★ 2026-09-24：附赠条目只进创造栏 —— 战利品箱走 ManorVictoryLoot，那里只遍历 ENTRIES。 */
        for (Entry entry : CREATIVE_ONLY) {
            ItemStack stack = stack(entry);
            if (!stack.isEmpty()) output.accept(stack);
        }
    }
}
