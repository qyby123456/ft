package com.farmingtales.drinkanim.client;

import java.lang.reflect.Method;

/** Forge's hand event precedes Oculus's per-hand pass filter. */
final class ShaderHandPass {
    private record Access(Object renderer,Method active,Method solid) {}
    private static final Access ACCESS=find();
    private static boolean failed;
    private static Access find() {
        for(String root:new String[]{"net.irisshaders.iris","net.coderbot.iris"}) {
            try {
                var type=Class.forName(root+".pathways.HandRenderer");
                return new Access(type.getField("INSTANCE").get(null),type.getMethod("isActive"),type.getMethod("isRenderingSolid"));
            } catch(ClassNotFoundException absent) {
                // Optional shader integration; vanilla rendering needs no pass filter.
            } catch(ReflectiveOperationException incompatible) {
                com.mojang.logging.LogUtils.getLogger().warn("Unable to query the shader hand pass",incompatible);
                return null;
            }
        }
        return null;
    }
    static boolean renderNow() {
        if(ACCESS==null||failed)return true;
        try {
            return !(Boolean)ACCESS.active.invoke(ACCESS.renderer)||(Boolean)ACCESS.solid.invoke(ACCESS.renderer);
        } catch(ReflectiveOperationException e) {
            failed=true;
            com.mojang.logging.LogUtils.getLogger().warn("Unable to query the shader hand pass",e);
            return true;
        }
    }
    private ShaderHandPass() {}
}
