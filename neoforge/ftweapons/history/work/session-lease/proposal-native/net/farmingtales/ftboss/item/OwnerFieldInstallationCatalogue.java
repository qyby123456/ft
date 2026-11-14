package net.farmingtales.ftboss.item;

final class OwnerFieldInstallationCatalogue {
    private static final String[][] HANDLERS = new String[][]{
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin",
                "ftboss$ownerTargetReceiver",
                "(Lnet/minecraft/world/entity/LivingEntity;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin",
                "ftboss$ownerTargetWrite",
                "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin",
                "ftboss$r2$timerPut",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin",
                "ftboss$r2$timerPutSrg",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin",
                "ftboss$ownerLocalDataCopy",
                "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin",
                "ftboss$ownerLocalBaselineCopy",
                "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerSemanticConsumerMixin",
                "ftboss$ownerSemanticLeaf",
                "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerRangedGuardMixin",
                "ftboss$ownerMaterialGuard",
                "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)Z"
        },
        {"net.farmingtales.ftboss.mixin.NativeExpectedActorFieldsMixin", "ftboss$rawPersistentData", "()Lnet/minecraft/nbt/CompoundTag;"},
        {
                "net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin",
                "ftboss$ownerFieldConstructed",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin",
                "ftboss$ownerFieldPersistentExposed",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {"net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin", "ftboss$ownerFieldRawLevel", "()Lnet/minecraft/world/level/Level;"},
        {
                "net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin",
                "ftboss$ownerFieldDomain",
                "()Lnet/farmingtales/ftboss/item/OwnerFieldExpectedAuthority$DomainBefore;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin",
                "ftboss$ownerFieldInstallDomain",
                "(Lnet/farmingtales/ftboss/item/OwnerFieldExpectedAuthority$DomainBefore;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin",
                "ftboss$ownerFieldLoadQuoted",
                "(Lnet/minecraft/nbt/CompoundTag;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin",
                "ftboss$ownerFieldLoadAssigned",
                "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {"net.farmingtales.ftboss.mixin.OwnerFieldSourceAccessMixin", "ftboss$ownerFieldDirectEntity", "()Lnet/minecraft/world/entity/Entity;"},
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairByteArrayTagMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairByteArrayTagMixin",
                "ftboss$fullRepairReturnedMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairByteArrayTagMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityDispatcherMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityDispatcherMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityProviderMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityProviderMixin",
                "ftboss$fullRepairReturnedMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityProviderMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin",
                "ftboss$fullRepairReturnedMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairIntArrayTagMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairIntArrayTagMixin",
                "ftboss$fullRepairReturnedMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairIntArrayTagMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin",
                "ftboss$fullRepairReturnedMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin",
                "ftboss$fullRepairGetOrCreateTag",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin",
                "ftboss$fullRepairGetOrCreateElement",
                "(Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairListTagMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairListTagMixin",
                "ftboss$fullRepairReturnedMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairListTagMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairLongArrayTagMixin",
                "ftboss$fullRepairVoidMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairLongArrayTagMixin",
                "ftboss$fullRepairReturnedMutation",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairLongArrayTagMixin",
                "ftboss$fullRepairAlias",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {"net.farmingtales.ftboss.mixin.OwnerNativeMaterialEntityIdentityMixin", "ftboss$ownerMaterialEntityIdentity", "()Ljava/util/UUID;"},
        {"net.farmingtales.ftboss.mixin.OwnerRepairCapabilityFieldsMixin", "ftboss$ownerRepairInitializedNoProviders", "()Z"},
        {"net.farmingtales.ftboss.mixin.OwnerRepairStackFieldsMixin", "ftboss$ownerRepairRawItem", "()Lnet/minecraft/world/item/Item;"},
        {"net.farmingtales.ftboss.mixin.OwnerRepairStackFieldsMixin", "ftboss$ownerRepairRawCapNbtAbsent", "()Z"},
        {"net.farmingtales.ftboss.mixin.OwnerRepairStackFieldsMixin", "ftboss$ownerRepairWasRetired", "()Z"},
        {"net.farmingtales.ftboss.mixin.OwnerRepairStackFieldsMixin", "ftboss$ownerRepairRetire", "()V"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedByteArrayTagFieldsMixin", "ftboss$rawBytes", "()[B"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedByteTagFieldsMixin", "ftboss$rawByte", "()B"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedCompoundTagFieldsMixin", "ftboss$rawCompoundFields", "()Ljava/util/Map;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedDoubleTagFieldsMixin", "ftboss$rawDouble", "()D"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedFloatTagFieldsMixin", "ftboss$rawFloat", "()F"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedIntArrayTagFieldsMixin", "ftboss$rawInts", "()[I"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedIntTagFieldsMixin", "ftboss$rawInt", "()I"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedListTagFieldsMixin", "ftboss$rawListFields", "()Ljava/util/List;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedListTagFieldsMixin", "ftboss$rawListKind", "()B"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedLongArrayTagFieldsMixin", "ftboss$rawLongs", "()[J"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedLongTagFieldsMixin", "ftboss$rawLong", "()J"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "ftboss$rawDispatcher", "()Ljava/lang/Object;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "ftboss$rawCapsValid", "()Z"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "ftboss$rawCapsLazy", "()Z"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "ftboss$rawCapsInitialized", "()Z"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "ftboss$rawCapsParentSupplier", "()Ljava/lang/Object;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "ftboss$rawCapsLazyData", "()Lnet/minecraft/nbt/CompoundTag;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedShortTagFieldsMixin", "ftboss$rawShort", "()S"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedStackFieldsMixin", "ftboss$rawStackTag", "()Lnet/minecraft/nbt/CompoundTag;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedStackFieldsMixin", "ftboss$rawStackCaps", "()Lnet/minecraft/nbt/CompoundTag;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedStackFieldsMixin", "ftboss$rawStackCount", "()I"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedStackFieldsMixin", "ftboss$rawStackItem", "()Lnet/minecraft/world/item/Item;"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedStringTagFieldsMixin", "ftboss$rawText", "()Ljava/lang/String;"},
        {"net.farmingtales.ftboss.mixin.OwnerOriginalPublicationFieldsMixin", "ftboss$ownerOriginalProducers", "()Ljava/util/List;"},
        {"net.farmingtales.ftboss.mixin.OwnerEvolutionPlanFieldsMixin", "ftboss$ownerOriginalPlan", "()Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan;"},
        {"net.farmingtales.ftboss.mixin.OwnerOriginalPlanFieldsMixin", "ftboss$ownerOriginalLeaves", "()Ljava/util/List;"},
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerQuoteMixin",
                "ftboss$ownerTimerQuoted",
                "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerQuoteMixin",
                "ftboss$ownerTimerQuote",
                "()Lnet/farmingtales/ftboss/item/OwnerFieldExpectedAuthority$TimerQuote;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerOriginalPublicationAdmissionMixin",
                "ftboss$ownerOriginalPublicationAssigned",
                "(Ljava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerOriginalRepairContinuationMixin",
                "ftboss$ownerRepairGetter",
                "(Lnet/minecraft/world/item/ItemStack;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Ljava/lang/Object;Ljava/lang/Object;)I"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerOriginalRepairContinuationMixin",
                "ftboss$ownerRepairSetter",
                "(Lnet/minecraft/world/item/ItemStack;ILcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",
                "ftboss$ownerTridentPickupTaken",
                "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;ILcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",
                "ftboss$ownerTridentPickup",
                "(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/world/entity/player/Player;)Z"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",
                "ftboss$ownerTridentOriginalCopy",
                "(Lnet/minecraft/world/item/ItemStack;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/world/item/ItemStack;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",
                "ftboss$ownerTridentLineage",
                "()Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$TridentCarrierLineage;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",
                "ftboss$ownerInstallTridentLineage",
                "(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$TridentCarrierLineage;)V"
        },
        {"net.farmingtales.ftboss.mixin.NativeTridentFieldCarrierMixin", "ftboss$originalTridentStack", "()Lnet/minecraft/world/item/ItemStack;"},
        {
                "net.farmingtales.ftboss.mixin.OwnerSnapshotLeaseCopyMixin",
                "ftboss$ownerSnapshotCopy",
                "(Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "ftboss$ownerRevocationNode", "()Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard$Node;"},
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "ftboss$ownerInstallRevocationNode",
                "(Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard$Node;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin",
                "ftboss$ownerRawExposed",
                "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedActualStepMixin",
                "ftboss$actualReturnedTimerStep",
                "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedAliasReturnMixin",
                "ftboss$originalAliasReturned",
                "(Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedCaptureBoundaryMixin",
                "ftboss$originalCapture",
                "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;Lnet/minecraft/world/InteractionHand;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedMapWriteMixin",
                "ftboss$originalFieldMapPut",
                "(Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Ljava/lang/Object;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedProviderObservationMixin",
                "ftboss$originalProviderBefore",
                "(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedProviderObservationMixin",
                "ftboss$originalProviderAfter",
                "(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedScopeMixin",
                "ftboss$originalRangedScope",
                "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Ljava/lang/Object;Ljava/lang/Object;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "ftboss$originalRangedPersistentData",
                "(Lnet/minecraft/server/level/ServerPlayer;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "ftboss$originalRangedCompound",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "ftboss$originalRangedCompoundSrg",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "ftboss$originalRangedLong",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;JLcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "ftboss$originalRangedLongSrg",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;JLcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "ftboss$originalRangedRoot",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "ftboss$originalRangedRootSrg",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedTagAssignmentMixin",
                "ftboss$originalTagAssigned",
                "(Lnet/minecraft/nbt/CompoundTag;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin",
                "ftboss$actualOriginalCopy",
                "(Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin",
                "ftboss$actualOriginalCopySrg",
                "(Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedTimerReceiverMixin",
                "ftboss$actualOriginalReceiver",
                "(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedTimerScopeMixin",
                "ftboss$originalTimerScope",
                "(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativeWeaponInputMixin",
                "ftboss$ownerNativeWeaponInput",
                "()Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$WeaponInput;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativeWeaponInputMixin",
                "ftboss$ownerInstallNativeWeaponInput",
                "(Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$WeaponInput;)V"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativeWeaponInputMixin",
                "ftboss$ownerNativeLoadedInput",
                "(Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Lcom/mojang/serialization/DataResult;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativeLeafFieldsMixin",
                "ftboss$ownerNativeAttempt",
                "()Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin",
                "ftboss$ownerNativePutQuote",
                "()Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$PutQuote;"
        },
        {"net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin", "ftboss$ownerNativeOriginalValue", "()Lnet/minecraft/nbt/Tag;"},
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin",
                "ftboss$ownerNativePutQuoted",
                "(Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin",
                "ftboss$ownerNativeCopy",
                "(Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Ljava/lang/Object;Ljava/lang/Object;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin",
                "ftboss$ownerNativePut",
                "(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/Tag;"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerInventoryTickContinuityMixin",
                "ftboss$ownerInventoryTickCallback",
                "(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZLcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V"
        }
    };

    private OwnerFieldInstallationCatalogue() {
    }

    static String descriptor(String var0, String var1) {
        for (String[] var5 : HANDLERS) {
            if (var5[0].equals(var0) && var5[1].equals(var1)) {
                return var5[2];
            }
        }

        return null;
    }
}
