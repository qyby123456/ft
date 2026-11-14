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
      this.m_21557_(true);
      this.m_20242_(true);
      this.m_6593_(Component.m_237113_("竹鸟"));
      this.m_20340_(true);
   }

   public static Builder attributes() {
      return Mob.m_21552_().m_22268_(Attributes.f_22276_, 1.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22278_, 1.0);
   }

   public UUID ownerId() {
      return this.owner;
   }

   public void setOwner(EasterBossEntity var1) {
      this.owner = var1.m_20148_();
   }

   public void revealedAt(long var1) {
      this.turretDrops.revealedAt(var1);
   }

   public void m_8119_() {
      super.m_8119_();
      if (this.m_9236_() instanceof ServerLevel var1) {
         if (this.owner != null
            && var1.m_8791_(this.owner) instanceof EasterBossEntity var8
            && var8.m_6084_()
            && !var8.m_213877_()
            && var8.getVariant() == 6
            && ManorBalanceHooks.mainBoss(var8)
            && ManorFightMembers.fightId(var8) != null
            && ManorFight.hasActive(ManorFightMembers.fightId(var8))) {
            this.m_21153_(1.0F);
            ZhuniStage.syncBossBar(var8);
            boolean var9 = ZhuniStage.waiting(var8);
            double var4 = (double)var1.m_46467_() * 0.04;
            Vec3 var6 = var9 ? ZhuniOpeningMovement.preferred(var8) : var8.m_20182_().m_82520_(Math.cos(var4) * 1.8, 2.8, Math.sin(var4) * 1.8);
            Vec3 var7 = var6.m_82546_(this.m_20182_()).m_82490_(0.2);
            this.m_20256_(Vec3.f_82478_);
            this.m_6478_(MoverType.SELF, var7);
            this.m_146922_((float)Math.toDegrees(Math.atan2(-var7.f_82479_, var7.f_82481_)));
            if (!var9 && !var8.isManorIntroHidden()) {
               this.turretDrops.tick(var8, var1);
               ZhuniBlackwallDisruption.tick(this, var8, var1);
            } else {
               this.revealedAt(var1.m_46467_());
            }
         } else {
            this.m_146870_();
         }
      }
   }

   public boolean m_6469_(DamageSource var1, float var2) {
      if (this.m_9236_() instanceof ServerLevel var3
         && Float.isFinite(var2)
         && !(var2 <= 0.0F)
         && this.owner != null
         && var3.m_8791_(this.owner) instanceof EasterBossEntity var4
         && var1.m_7639_() instanceof ServerPlayer var5
         && ManorBalanceHooks.players(var4, var3).contains(var5)) {
         boolean var10 = ZhuniStage.waiting(var4);
         ZhuniStage.reveal(var4, this);
         this.m_21153_(1.0F);
         boolean var7 = var4.m_6469_(var1, var2);
         if (var4.m_6084_()) {
            ZhuniStage.syncBossBar(var4);
         } else {
            this.m_146870_();
         }

         return var7 || var10;
      }

      return false;
   }

   public void m_7380_(CompoundTag var1) {
      super.m_7380_(var1);
      if (this.owner != null) {
         var1.m_128362_("Owner", this.owner);
      }
   }

   public void m_7378_(CompoundTag var1) {
      super.m_7378_(var1);
      this.owner = var1.m_128403_("Owner") ? var1.m_128342_("Owner") : null;
      this.revealedAt(this.m_9236_().m_46467_());
   }
}
