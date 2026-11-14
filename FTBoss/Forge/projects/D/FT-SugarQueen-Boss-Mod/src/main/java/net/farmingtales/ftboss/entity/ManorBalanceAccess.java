package net.farmingtales.ftboss.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/** 保留正式紫菜单体结算并供多人分发调用的桥接接口。 */
public interface ManorBalanceAccess {
    void ftboss$applyPufferSingle(ServerLevel level, LivingEntity target);
}
