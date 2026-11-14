package ftboss.clientfxprobe.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import ftboss.clientfxprobe.BossClientRenderMatrixProbe;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityDispatcherReturnObserver {
    @Inject(method="render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("RETURN"))
    private <T extends Entity> void ftbossObservedOriginalRendererReturn(T entity,double horizontal,double vertical,double depth,float yaw,float partialTick,PoseStack poses,MultiBufferSource buffers,int light,CallbackInfo callback) {
        BossClientRenderMatrixProbe.entityDispatcherReturned(entity);
    }
}
