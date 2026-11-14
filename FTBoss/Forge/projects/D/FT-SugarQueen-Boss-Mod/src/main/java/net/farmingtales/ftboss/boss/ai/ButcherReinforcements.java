package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class ButcherReinforcements {
    private ButcherReinforcements() {}

    public static void summon(MeatMountainEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) return;
        int spawned = 0;
        for (int attempt = 0; attempt < 24 && spawned < ButcherBalanceRules.OPENING_MINIONS; attempt++) {
            var minion = EntityType.VINDICATOR.create(level);
            if (minion == null) return;
            double angle = attempt * Math.PI / 4;
            double radius = 3 + attempt / 8;
            double positionX = boss.getX() + Math.cos(angle) * radius;
            double positionZ = boss.getZ() + Math.sin(angle) * radius;
            boolean placed = false;
            for (int offset = 2; offset >= -4; offset--) {
                var ground = net.minecraft.core.BlockPos.containing(positionX, boss.getY() + offset, positionZ);
                if (!level.getBlockState(ground.below()).isSolidRender(level, ground.below())) continue;
                minion.moveTo(positionX, ground.getY(), positionZ, boss.getYRot(), 0);
                if (level.noCollision(minion)) { placed = true; break; }
            }
            if (!placed) continue;
            minion.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ButcherCombatRules.minionHealth(0));
            minion.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ButcherCombatRules.minionDamage(0));
            minion.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(ButcherCombatRules.minionSpeed(0));
            minion.setHealth(minion.getMaxHealth());
            minion.setCanPickUpLoot(false);
            minion.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(FTBossItems.TURKEY_LEGS.get()));
            minion.setDropChance(EquipmentSlot.MAINHAND, 0);
            minion.goalSelector.addGoal(1, new ButcherMinionMeleeGoal(minion, 0));
            MinionBlessing.mark(minion, boss.getUUID());
            minion.setTarget(boss.getTarget());
            if (net.farmingtales.ftboss.butcherskills.ButcherSkills.addWithFx(level, minion)) spawned++;
        }
    }
}
