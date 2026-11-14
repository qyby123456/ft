/** 手持武器空路径性能补丁的移植或回归工具，2026-10-20。 */
public final class LoadVerify {
    public static void main(String[] arguments) throws Exception {
        for (String name : new String[]{"OwnerRepairContinuation", "OwnerStatueWearAdmission", "NativeFoodWeaponWear", "FoodWeaponEvolution", "FoodSocketPowers", "FoodWeaponLifecycle"}) {
            Class<?> type = Class.forName("net.farmingtales.ftboss.item." + name, false, LoadVerify.class.getClassLoader());
            System.out.println("VERIFIED " + type.getName() + " methods=" + type.getDeclaredMethods().length);
        }
    }
}
