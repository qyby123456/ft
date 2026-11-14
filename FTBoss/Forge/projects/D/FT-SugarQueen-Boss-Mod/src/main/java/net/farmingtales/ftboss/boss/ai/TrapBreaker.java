package net.farmingtales.ftboss.boss.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.phys.AABB;

/**
 * 「破困」: 全部 Boss 每 10 tick 自查一次, 把自己身上/身下的**困人东西**直接清掉。
 *
 * <p>清两类:
 * <ol>
 *   <li><b>载具</b> —— {@link AbstractMinecart}(各类矿车) 与 {@link Boat}(船/箱船): 卡在 Boss 碰撞箱里的直接移除;</li>
 *   <li><b>坐垫类方块</b> —— 注册名里含 {@link #KEYWORDS 坐垫/座椅关键词} 的(本整合包里实际命中的是
 *       {@code create:*_seat}、{@code handcrafted:*_cushion}、{@code redeco:*_cushion}、
 *       {@code dustydecorations:*_cushion}、{@code yuushya:seat_*}/{@code cushion_armchair_*}、
 *       {@code furniture:pouffe_*}), 外加本模组的方块标签 {@code ftboss:trap_breakable}(留作手动追加的入口,
 *       默认空表), 以及原版蜘蛛网/细雪。</li>
 * </ol>
 * 之所以要清: 这些方块/载具会把实体卡住, Boss 又不会自己绕开 —— 被玩家用来"卡住 Boss"就没法打了。
 */
public final class TrapBreaker {

    /** 可扩展的方块标签: data/ftboss/tags/blocks/trap_breakable.json (默认空表, 需要时手加方块 id) */
    public static final TagKey<Block> TRAP_TAG =
            BlockTags.create(new ResourceLocation("ftboss", "trap_breakable"));

    /**
     * 注册名关键词(命中即视为"坐垫/座椅"这类能困住实体的软家具)。
     *
     * <p>2026-09-10 逐个 jar 扫过整合包 (347 个 mod) 的 blockstates 与 zh_cn 语言文件, 实测命中:
     * 中文名叫"坐垫"的只有 4 个命名空间 —— {@code create:*_seat}(机械动力)、{@code handcrafted:*_cushion}(精巧手艺)、
     * {@code redeco:*_cushion}(装饰)、{@code dustydecorations:*_cushion}(封尘装饰, 含 _slab/_stairs);
     * 另有 {@code yuushya:seat_*}/{@code yuushya:cushion_armchair_*}(方块小镇)、{@code furniture:pouffe_*}(Let's Do 家具)
     * 等英文名坐具。<b>注意</b>: 之前以为存在的 {@code furniture_mod}/{@code valhelsia_furniture} 在本整合包里并没有装,
     * 所以标签表清空成 {@code []}, 全靠关键词自动识别(新装 mod 也能自动覆盖)。
     * 这里刻意<b>不</b>收 {@code stool}/{@code bench}/{@code table} 这类硬家具(几百个方块, 且困不住实体),
     * 只收"能坐进去/陷进去"的软家具, 免得把玩家的装饰基地拆光。
     */
    private static final String[] KEYWORDS = {
            "cushion", "pillow", "seat",          // 坐垫/枕头/座椅(用户点名的"坐垫"就是这一类)
            "sofa", "couch", "chair", "armchair", // 沙发/扶手椅
            "ottoman", "pouf", "beanbag", "hassock", "futon", "hammock"
    };

    /** 清理间隔(tick) */
    private static final int PERIOD = 10;

    private TrapBreaker() {}

    /** 每 tick 调用(服务端, 由各 Boss 的 tick 转发) */
    public static void tick(LivingEntity boss) {
        if (boss.level().isClientSide() || boss.isDeadOrDying()) return;
        if (boss.tickCount % PERIOD != 0) return;
        if (!(boss.level() instanceof ServerLevel sl)) return;

        boolean did = false;

        // ① 卡在身上的载具(矿车/船) —— 直接把乘客请出来再移除
        AABB tight = boss.getBoundingBox().inflate(0.35);
        for (Entity e : sl.getEntities(boss, tight)) {
            if (e instanceof AbstractMinecart || e instanceof Boat) {
                for (Entity passenger : e.getPassengers()) {
                    passenger.stopRiding();
                    if (passenger instanceof Player p) {
                        p.displayClientMessage(net.minecraft.network.chat.Component.literal("§7你的载具被 Boss 掀翻了"), true);
                    }
                }
                e.discard();
                did = true;
            }
        }

        // ② 困人方块(坐垫/枕头/座位/蜘蛛网)
        AABB box = boss.getBoundingBox().inflate(0.12, 0.05, 0.12);
        int minX = (int) Math.floor(box.minX), maxX = (int) Math.ceil(box.maxX);
        int minY = (int) Math.floor(box.minY), maxY = (int) Math.ceil(box.maxY);
        int minZ = (int) Math.floor(box.minZ), maxZ = (int) Math.ceil(box.maxZ);
        for (BlockPos p : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockState st = sl.getBlockState(p);
            if (!isTrap(st)) continue;
            sl.destroyBlock(p.immutable(), false);   // 不掉落, 免得刷物品
            did = true;
        }

        if (did) {
            sl.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 0.7f, 0.8f);
        }
    }

    /** 是否属于"会困住实体"的方块 */
    public static boolean isTrap(BlockState st) {
        if (st.isAir()) return false;
        if (st.is(Blocks.COBWEB) || st.is(Blocks.POWDER_SNOW)) return true;
        if (st.is(TRAP_TAG)) return true;
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(st.getBlock());
        if (key == null) return false;
        String path = key.getPath().toLowerCase(java.util.Locale.ROOT);
        for (String k : KEYWORDS) {
            if (path.contains(k)) return true;
        }
        return false;
    }
}
