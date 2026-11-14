package net.farmingtales.ftboss.client.render.bloom;

import java.lang.reflect.Method;

/** 可选光影接口探针；2026-10-27 缓存接口查找，启用状态仍逐次读取。 */
public final class ShaderPackProbe {
    private final Method instanceMethod;
    private final Method activeMethod;
    private final boolean unavailable;

    ShaderPackProbe(ClassLoader loader, String... types) {
        Method instance = null;
        Method active = null;
        boolean failed = false;
        for (String type : types) {
            try {
                Class<?> api = Class.forName(type, false, loader);
                instance = api.getMethod("getInstance");
                active = api.getMethod("isShaderPackInUse");
                break;
            } catch (ClassNotFoundException absent) {
                continue;
            } catch (ReflectiveOperationException | LinkageError exception) {
                failed = true;
                break;
            }
        }
        instanceMethod = instance;
        activeMethod = active;
        unavailable = failed;
    }

    public static boolean active() {
        return DefaultProbe.INSTANCE.isActive();
    }

    boolean isActive() {
        if (unavailable) return true;
        if (instanceMethod == null) return false;
        try {
            return (boolean) activeMethod.invoke(instanceMethod.invoke(null));
        } catch (ReflectiveOperationException | LinkageError exception) {
            return true;
        }
    }

    private static final class DefaultProbe {
        private static final ShaderPackProbe INSTANCE = new ShaderPackProbe(ShaderPackProbe.class.getClassLoader(),
                "net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi");
    }
}
