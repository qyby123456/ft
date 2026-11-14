/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.vehicle.AbstractMinecart
 *  net.minecraft.world.entity.vehicle.Boat
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public final class TrapBreaker {
    public static final TagKey<Block> TRAP_TAG = BlockTags.create((ResourceLocation)new ResourceLocation("ftboss", "trap_breakable"));
    private static final String[] KEYWORDS = new String[]{"cushion", "pillow", "seat", "sofa", "couch", "chair", "armchair", "ottoman", "pouf", "beanbag", "hassock", "futon", "hammock"};
    private static final int PERIOD = 10;

    private TrapBreaker() {
    }

    public static void tick(LivingEntity boss) {
        if (boss.m_9236_().m_5776_() || boss.m_21224_()) {
            return;
        }
        if (boss.f_19797_ % 10 != 0) {
            return;
        }
        Level level = boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        boolean did = false;
        AABB tight = boss.m_20191_().m_82400_(0.35);
        for (Entity e : sl.m_45933_((Entity)boss, tight)) {
            if (!(e instanceof AbstractMinecart) && !(e instanceof Boat)) continue;
            for (Entity passenger : e.m_20197_()) {
                passenger.m_8127_();
                if (!(passenger instanceof Player)) continue;
                Player p = (Player)passenger;
                p.m_5661_((Component)Component.m_237113_((String)"\u00a77\u4f60\u7684\u8f7d\u5177\u88ab Boss \u6380\u7ffb\u4e86"), true);
            }
            e.m_146870_();
            did = true;
        }
        AABB box = boss.m_20191_().m_82377_(0.12, 0.05, 0.12);
        int minX = (int)Math.floor(box.f_82288_);
        int maxX = (int)Math.ceil(box.f_82291_);
        int minY = (int)Math.floor(box.f_82289_);
        int maxY = (int)Math.ceil(box.f_82292_);
        int minZ = (int)Math.floor(box.f_82290_);
        int maxZ = (int)Math.ceil(box.f_82293_);
        for (BlockPos p : BlockPos.m_121976_((int)minX, (int)minY, (int)minZ, (int)maxX, (int)maxY, (int)maxZ)) {
            BlockState st = sl.m_8055_(p);
            if (!TrapBreaker.isTrap(st)) continue;
            sl.m_46961_(p.m_7949_(), false);
            did = true;
        }
        if (did) {
            sl.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_12018_, SoundSource.HOSTILE, 0.7f, 0.8f);
        }
    }

    public static boolean isTrap(BlockState st) {
        if (st.m_60795_()) {
            return false;
        }
        if (st.m_60713_(Blocks.f_50033_) || st.m_60713_(Blocks.f_152499_)) {
            return true;
        }
        if (st.m_204336_(TRAP_TAG)) {
            return true;
        }
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey((Object)st.m_60734_());
        if (key == null) {
            return false;
        }
        String path = key.m_135815_().toLowerCase(Locale.ROOT);
        for (String k : KEYWORDS) {
            if (!path.contains(k)) continue;
            return true;
        }
        return false;
    }
}

