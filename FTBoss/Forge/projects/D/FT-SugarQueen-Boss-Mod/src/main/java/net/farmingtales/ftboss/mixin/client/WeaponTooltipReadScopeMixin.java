package net.farmingtales.ftboss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.List;
import net.farmingtales.ftboss.item.WeaponTooltipReadScope;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;

/** 客户端描述生成边界；2026-10-25 覆盖属性和事件提示，并在正常或异常退出时清除读取快照。 */
@Mixin(ItemStack.class)
public abstract class WeaponTooltipReadScopeMixin {
    @WrapMethod(method = {
            "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            "m_41651_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"
    }, remap = false)
    private List<Component> ftboss$tooltipReadScope(Player player, TooltipFlag flags, Operation<List<Component>> original) {
        return WeaponTooltipReadScope.tooltip(() -> original.call(player, flags));
    }
}
