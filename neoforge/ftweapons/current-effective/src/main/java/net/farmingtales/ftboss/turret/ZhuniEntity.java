package net.farmingtales.ftboss.turret;

import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ZhuniBlackwallDisruption;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ZhuniEntity extends Mob {
    private UUID owner;
    private long nextDrop;
    private final ZhuniTurretDrops turretDrops = new ZhuniTurretDrops();

    public ZhuniEntity(EntityType<? extends ZhuniEntity> var1, Level var2) {
        super(var1, var2);
        this.setNoAi(true);
        this.setNoGravity(true);
        this.setCustomName(Component.literal("竹鸟"));
        this.setCustomNameVisible(true);
    }

    public static Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public UUID ownerId() {
        return this.owner;
    }

    public void setOwner(EasterBossEntity var1) {
        this.owner = var1.getUUID();
    }

    public void revealedAt(long var1) {
        this.turretDrops.revealedAt(var1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel var1) {
            if (this.owner != null
                && var1.getEntity(this.owner) instanceof EasterBossEntity var8
                && var8.isAlive()
                && !var8.isRemoved()
                && var8.getVariant() == 6
                && ManorBalanceHooks.mainBoss(var8)
                && ManorFightMembers.fightId(var8) != null
                && ManorFight.hasActive(ManorFightMembers.fightId(var8))) {
                this.setHealth(1.0F);
                ZhuniStage.syncBossBar(var8);
                boolean var9 = ZhuniStage.waiting(var8);
                double var4 = (double)var1.getGameTime() * 0.04;
                Vec3 var6 = var9 ? ZhuniOpeningMovement.preferred(var8) : var8.position().add(Math.cos(var4) * 1.8, 2.8, Math.sin(var4) * 1.8);
                Vec3 var7 = var6.subtract(this.position()).scale(0.2);
                this.setDeltaMovement(Vec3.ZERO);
                this.move(MoverType.SELF, var7);
                this.setYRot((float)Math.toDegrees(Math.atan2(-var7.x, var7.z)));
                if (!var9 && !var8.isManorIntroHidden()) {
                    this.turretDrops.tick(var8, var1);
                    ZhuniBlackwallDisruption.tick(this, var8, var1);
                } else {
                    this.revealedAt(var1.getGameTime());
                }
            } else {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource var1, float var2) {
        if (this.level() instanceof ServerLevel var3
            && Float.isFinite(var2)
            && !(var2 <= 0.0F)
            && this.owner != null
            && var3.getEntity(this.owner) instanceof EasterBossEntity var4
            && var1.getEntity() instanceof ServerPlayer var5
            && ManorBalanceHooks.players(var4, var3).contains(var5)) {
            boolean var10 = ZhuniStage.waiting(var4);
            ZhuniStage.reveal(var4, this);
            this.setHealth(1.0F);
            boolean var7 = var4.hurt(var1, var2);
            if (var4.isAlive()) {
                ZhuniStage.syncBossBar(var4);
            } else {
                this.discard();
            }

            return var7 || var10;
        }

        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag var1) {
        super.addAdditionalSaveData(var1);
        if (this.owner != null) {
            var1.putUUID("Owner", this.owner);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag var1) {
        super.readAdditionalSaveData(var1);
        this.owner = var1.hasUUID("Owner") ? var1.getUUID("Owner") : null;
        this.revealedAt(this.level().getGameTime());
    }
}
