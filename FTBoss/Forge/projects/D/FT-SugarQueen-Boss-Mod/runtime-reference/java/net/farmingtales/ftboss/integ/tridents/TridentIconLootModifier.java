/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.storage.loot.LootContext
 *  net.minecraft.world.level.storage.loot.predicates.LootItemCondition
 *  net.minecraftforge.common.loot.IGlobalLootModifier
 *  net.minecraftforge.common.loot.LootModifier
 */
package net.farmingtales.ftboss.integ.tridents;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.farmingtales.ftboss.integ.tridents.TridentIntegration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public final class TridentIconLootModifier
extends LootModifier {
    public static final Codec<TridentIconLootModifier> CODEC = RecordCodecBuilder.create(instance -> TridentIconLootModifier.codecStart((RecordCodecBuilder.Instance)instance).apply((Applicative)instance, TridentIconLootModifier::new));

    public TridentIconLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.removeIf(TridentIntegration::isDisabledIcon);
        return generatedLoot;
    }

    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

