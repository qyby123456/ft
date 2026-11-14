/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.minecraft.world.level.Level;

public interface OwnerFieldEntityAccess {
    public Level ftboss$ownerFieldRawLevel();

    public OwnerFieldExpectedAuthority.DomainBefore ftboss$ownerFieldDomain();

    public void ftboss$ownerFieldInstallDomain(OwnerFieldExpectedAuthority.DomainBefore var1);
}

