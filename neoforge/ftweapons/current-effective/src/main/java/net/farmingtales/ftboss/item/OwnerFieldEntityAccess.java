package net.farmingtales.ftboss.item;

import net.minecraft.world.level.Level;

public interface OwnerFieldEntityAccess {
    Level ftboss$ownerFieldRawLevel();

    OwnerFieldExpectedAuthority.DomainBefore ftboss$ownerFieldDomain();

    void ftboss$ownerFieldInstallDomain(OwnerFieldExpectedAuthority.DomainBefore var1);
}
