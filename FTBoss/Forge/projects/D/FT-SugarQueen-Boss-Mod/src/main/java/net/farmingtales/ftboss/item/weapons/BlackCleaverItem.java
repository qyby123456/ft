package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.BlackCleaverGeoRenderer;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * 【黑色切割者 · 赤痕】GeckoLib 骨骼物品（玩法仍继承 {@link BlackCleaverAndShadow.BlackCleaver}，只换渲染）。
 *
 * <p>这一层只做三件事，都是"血怒展开"表现所需的：
 * <ol>
 *   <li><b>把斧头 geo 化</b>：{@code initializeClient} 返回 {@link BlackCleaverGeoRenderer}
 *       （geo/black_cleaver.geo.json + animations/black_cleaver.animation.json +
 *        <b>textures/item/black_cleaver_geo.png</b>）。</li>
 *   <li><b>服务端把"血怒开关 + 层数"写进 ItemStack 的 NBT</b>（{@value #TAG_BLOOD} / {@value #TAG_BLOOD_LV}）——
 *       跟着背包同步下发，于是渲染器读的是<b>这把斧头自己的</b>状态：
 *       两把斧同时持有都各自展开 ✓，别人手里的斧、掉落物、展示框不会跟随本地玩家 ✓。
 *       （项目既有的同款范式：{@code ShadowSwordItem} 的 {@code ftboss_ready}。）
 *       <b>不在 Item 单例里保存任何玩家状态</b>。</li>
 *   <li><b>血怒粒子</b>：服务端每 5 tick 最多 1 个（{@link #PARTICLE_INTERVAL}），
 *       由 {@code ServerLevel.sendParticles} 广播 —— 自带 32 格距离上限，旁观者进范围就能看见。
 *       <b>不在 render 回调里生成</b>。</li>
 * </ol>
 *
 * <p>渲染侧的骨骼偏移（两片刃翼 ±8°、8 tick 开合、按持有者 UUID/每把斧独立、不累加、
 * 共享烘焙模型用完还原）在 {@code client/render/CleaverBloodVisualState} +
 * {@link BlackCleaverGeoRenderer#renderRecursively} 里，本类不碰。
 *
 * <p>★ 贴图红线：{@code textures/item/black_cleaver.png} 是 gen_weapons3d.cjs 出的 32×32 平面物品图标，
 * 被 {@code models/item/black_cleaver.json} 逐面 UV 引用 —— 本类与生成器都只用 {@code black_cleaver_geo.png}。
 */
public class BlackCleaverItem extends BlackCleaverAndShadow.BlackCleaver implements GeoItem {

    /** ItemStack NBT：true = 持有者此刻带【血怒·伪】。 */
    public static final String TAG_BLOOD = "ftboss_blood";
    /** ItemStack NBT：血怒层数（1..5）。发光/粒子强度用；0 = 无血怒。 */
    public static final String TAG_BLOOD_LV = "ftboss_blood_lv";

    /** 粒子节流：每 5 tick 最多 1 个。 */
    public static final int PARTICLE_INTERVAL = 5;

    private static final String CTRL = CleaverSkillController.NAME;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.black_cleaver.idle");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.black_cleaver.charge");
    private static final RawAnimation SLAM = RawAnimation.begin().thenPlay("animation.black_cleaver.slam").thenPlay("animation.black_cleaver.recover");
    private static final RawAnimation BLOOD_SLAM = RawAnimation.begin().thenPlay("animation.black_cleaver.blood_slam").thenPlay("animation.black_cleaver.blood_recover");
    private static final RawAnimation BLOOD_RECOVER = RawAnimation.begin().thenPlay("animation.black_cleaver.blood_recover");
    private static final RawAnimation RECOVER = RawAnimation.begin().thenPlay("animation.black_cleaver.recover");
    private static final RawAnimation READY = RawAnimation.begin().thenPlay("animation.black_cleaver.ready");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BlackCleaverItem() {
        super();
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "weapon_controller", 5,
                state -> state.setAndContinue(IDLE)));
        controllers.add(new CleaverSkillController(this)
                .triggerableAnim("charge", CHARGE)
                .triggerableAnim("slam", SLAM)
                .triggerableAnim("recover", RECOVER)
                .triggerableAnim("blood_slam", BLOOD_SLAM)
                .triggerableAnim("blood_recover", BLOOD_RECOVER)
                .triggerableAnim("ready", READY));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 服务端触发一次技能动画（与 ShadowSwordItem#triggerSkillAnim 同款）。 */
    public void triggerCleaverAnim(Entity holder, ItemStack stack, String name) {
        if (holder.level() instanceof ServerLevel sl) {
            boolean blood = holder instanceof net.minecraft.world.entity.LivingEntity living && living.hasEffect(FTEffects.BLOOD_RAGE_FAKE.get());
            String action = blood && (name.equals("slam") || name.equals("recover")) ? "blood_" + name : name;
            triggerAnim(holder, GeoItem.getOrAssignId(stack, sl), CTRL, action);
        }
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                     net.minecraft.world.InteractionHand hand) {
        return CleaverCombat.use(level, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof Player player) || !(level instanceof ServerLevel sl)) {
            return;                                   // 只服务端写；客户端读 NBT / 渲染
        }

        /* 血怒状态：从持有者身上读，写进这把斧头自己的 NBT（变更才写，避免每 tick 触发同步）。 */
        boolean blood = false;
        int stacks = 0;
        var eff = FTEffects.BLOOD_RAGE_FAKE.get();
        if (eff != null) {
            var inst = player.getEffect(eff);
            if (inst != null) {
                blood = true;
                stacks = inst.getAmplifier() + 1;     // 1..5
            }
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getBoolean(TAG_BLOOD) != blood) {
            tag.putBoolean(TAG_BLOOD, blood);
            /* 状态翻转打一条 —— 验收清单里"Buff 获得/消失""两人只有一人有 Buff"
               直接看这行就能分辨"没生效"还是"根本没触发"。 */
            FTBossMod.LOGGER.info("[FTBoss][黑切] {} 的血怒标记 {} -> {}（层数 {}）",
                    player.getName().getString(), !blood, blood, stacks);
        }
        if (tag.getInt(TAG_BLOOD_LV) != stacks) {
            tag.putInt(TAG_BLOOD_LV, stacks);
        }

        /* 保证这把斧头有 GeckoLib 的 per-stack 动画 id（渲染器用它做"每把斧独立"的进度键）。 */
        GeoItem.getOrAssignId(stack, sl);

        /* 血怒粒子：只在这一把真的被拿在手上时生成（主手选中 或 副手）。 */
        boolean held = selected || player.getOffhandItem() == stack;
        if (blood && held && sl.getGameTime() % PARTICLE_INTERVAL == 0) {
            spawnBloodParticle(sl, player, stacks);
        }
    }

    /** 1 个"血怒火星"，落在握持手前方的斧头位置；距 32 格外的玩家收不到（sendParticles 自带范围）。 */
    private static void spawnBloodParticle(ServerLevel sl, Player player, int stacks) {
        var look = player.getLookAngle();
        double x = player.getX() + look.x * 0.45D;
        double y = player.getY() + 1.05D + (player.getRandom().nextDouble() - 0.5D) * 0.35D;
        double z = player.getZ() + look.z * 0.45D;
        /* 层数越高越密一点点（仍然每 5 tick 只有这 1 个调用）。 */
        int count = stacks >= 5 ? 2 : 1;
        sl.sendParticles(ParticleTypes.CRIMSON_SPORE, x, y, z, count, 0.12D, 0.12D, 0.12D, 0.0D);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.black_cleaver.slam").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.black_cleaver.rage_slam").withStyle(ChatFormatting.DARK_RED));
        int stacks = (stack.hasTag() ? stack.getTag().getInt(TAG_BLOOD_LV) : 0);
        if (stacks > 0) {
            tooltip.add(Component.translatable("tooltip.ftboss.black_cleaver.blood", stacks)
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        FTBossMod.LOGGER.info("[FTBoss][黑切] 调用链① Item#initializeClient 被 Forge 调用了");
        consumer.accept(new IClientItemExtensions() {
            private BlackCleaverGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    FTBossMod.LOGGER.info("[FTBoss][黑切] 调用链② getCustomRenderer 被调用 -> 构造 GeoItemRenderer");
                    this.renderer = new BlackCleaverGeoRenderer();
                }
                return this.renderer;
            }
        });
    }
}
