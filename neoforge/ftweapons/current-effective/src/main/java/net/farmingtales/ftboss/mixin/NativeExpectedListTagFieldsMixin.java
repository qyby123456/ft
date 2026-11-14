package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Sequence;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    value = {ListTag.class},
    remap = false
)
public abstract class NativeExpectedListTagFieldsMixin implements Sequence {
    @Shadow(
        remap = false,
        aliases = {"list"}
    )
    @Final
    private List<Tag> list;
    @Shadow(
        remap = false,
        aliases = {"type"}
    )
    private byte type;

    @Override
    public List<Tag> ftboss$rawListFields() {
        return this.list;
    }

    @Override
    public byte ftboss$rawListKind() {
        return this.type;
    }
}
