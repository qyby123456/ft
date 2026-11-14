package net.farmingtales.ftboss.zheergenphantom;

import net.minecraft.server.level.ServerLevel;

public interface PhantomAccess {
    PhantomSnapshot ftboss$phantomSnapshot();

    void ftboss$phantomRestore(PhantomSnapshot var1);

    void ftboss$phantomClear();

    void ftboss$phantomReplay(ServerLevel var1);
}

