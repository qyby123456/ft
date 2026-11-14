/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 */
package net.farmingtales.ftboss.zheergenphantom;

import net.farmingtales.ftboss.zheergenphantom.PhantomSnapshot;
import net.minecraft.server.level.ServerLevel;

public interface PhantomAccess {
    public PhantomSnapshot ftboss$phantomSnapshot();

    public void ftboss$phantomRestore(PhantomSnapshot var1);

    public void ftboss$phantomClear();

    public void ftboss$phantomReplay(ServerLevel var1);
}

