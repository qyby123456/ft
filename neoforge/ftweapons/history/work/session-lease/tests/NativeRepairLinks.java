package net.farmingtales.ftboss.item;

public final class NativeRepairLinks {
    public static void main(String[] arguments) throws Exception {
        if (OwnerRepairGenerationAuthority.publishNativeRepair(null, null, null)) throw new AssertionError("Direct repair grant accepted");
        var method = OwnerRepairGenerationAuthority.class.getDeclaredMethod("publishNativeRepair", NativeFoodWeaponWear.Claim.class,
            net.minecraft.world.item.ItemStack.class, OwnerLineageRevocationStore.Lease.class);
        method.setAccessible(true);
        if (Boolean.TRUE.equals(method.invoke(null, new Object[3]))) throw new AssertionError("Reflective repair grant accepted");
        for (String name : arguments) {
            Class<?> linked = Class.forName(name, false, NativeRepairLinks.class.getClassLoader());
            linked.getDeclaredFields();
            linked.getDeclaredMethods();
            linked.getDeclaredConstructors();
        }
        System.out.println("linked=" + arguments.length + ", rejected=2");
    }
}
