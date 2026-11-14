package net.farmingtales.ftboss.item;
public final class ContinuationCallers {
    public static void main(String[] args) throws Throwable {
        var method = NativeExpectedFieldObservation.class.getDeclaredMethod("plannedRangedFieldMutation", Object.class, int.class);
        method.setAccessible(true);
        var handle = java.lang.invoke.MethodHandles.lookup().unreflect(method);
        int rejected = 0;
        for (int reason : new int[]{20,21,40,42}) {
            Object owner = new net.minecraft.nbt.CompoundTag();
            if (NativeExpectedFieldObservation.plannedRangedFieldMutation(owner, reason)) throw new AssertionError("Direct grant");
            rejected++;
            if (Boolean.TRUE.equals(method.invoke(null, owner, reason))) throw new AssertionError("Reflective grant");
            rejected++;
            if ((boolean) handle.invokeExact(owner, reason)) throw new AssertionError("Handle grant");
            rejected++;
        }
        if (rejected != 12) throw new AssertionError("Missing rejection");
        System.out.println("foreign and out-of-context mutations rejected=" + rejected);
    }
}
