package net.farmingtales.validation.weaponui;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** 弓弩真实客户端回归；2026-10-20 使用网络、自然充能 tick、弹药消耗及服务端实体回读。 */
public final class BowLoadProbe {
    private record Case(String item, InteractionHand hand, String condition) {}
    private final List<Case> cases = new ArrayList<>();
    private final List<Map<String, Object>> results = new ArrayList<>();
    private final Path instance;
    private final BlockPos statue;
    private final Runnable completed;
    private final Consumer<Throwable> failed;
    private volatile int phase;
    private int index;
    private int clientTicks;
    private int serverTicks;
    private int caseTicks;
    private String useResult;
    private int beforeDamage;
    private boolean charged;

    private BowLoadProbe(Path instance, BlockPos statue, Runnable completed, Consumer<Throwable> failed) {
        this.instance = instance;
        this.statue = statue;
        this.completed = completed;
        this.failed = failed;
        for (String item : List.of("minecraft:bow", "minecraft:crossbow")) {
            for (String condition : List.of("READY", "BROKEN", "INVALID_COUNT", "NO_AMMO", "UNDERCHARGED")) {
                for (InteractionHand hand : InteractionHand.values()) cases.add(new Case(item, hand, condition));
            }
        }
        for (String item : List.of("ftboss:wooden_bow", "ftboss:diamond_bow", "ftboss:wooden_crossbow", "ftboss:diamond_crossbow")) {
            for (InteractionHand hand : InteractionHand.values()) cases.add(new Case(item, hand, "READY"));
        }
    }

    public static void start(Minecraft client, Path instance, BlockPos statue, Runnable completed, Consumer<Throwable> failed) {
        client.player.closeContainer();
        client.setScreen(null);
        MinecraftForge.EVENT_BUS.register(new BowLoadProbe(instance, statue, completed, failed));
    }

    private boolean crossbow() { return cases.get(index).item().contains("crossbow"); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || phase < 0) return;
        try {
            require(++caseTicks < 500, "Bow timeout " + cases.get(index) + " phase=" + phase);
            ServerPlayer player = event.getServer().getPlayerList().getPlayers().get(0);
            Case test = cases.get(index);
            if (phase == 0) {
                player.closeContainer();
                player.setGameMode(GameType.CREATIVE);
                player.getInventory().clearContent();
                player.getInventory().selected = 0;
                player.setHealth(player.getMaxHealth());
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                player.teleportTo(statue.getX() - 1, statue.getY(), statue.getZ() + 0.5);
                player.setXRot(-35);
                player.setYRot(180);
                for (var entity : player.serverLevel().getAllEntities()) {
                    if (entity instanceof Projectile projectile && projectile.getOwner() == player) entity.discard();
                }
                var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(test.item()));
                require(item != null && item != Items.AIR, "Missing item " + test.item());
                ItemStack weapon = new ItemStack(item);
                player.setItemInHand(test.hand(), weapon);
                if (FourWeaponFoundationAdapter.applies(weapon)) require(FoundationEntryPoints.seedFromStatue(player, weapon, statue, true), "Seed failed");
                if (test.condition().equals("BROKEN")) weapon.setDamageValue(weapon.getMaxDamage());
                if (test.condition().equals("INVALID_COUNT")) weapon.setCount(2);
                if (!test.condition().equals("NO_AMMO")) player.getInventory().setItem(9, new ItemStack(Items.ARROW, 64));
                beforeDamage = weapon.getDamageValue();
                charged = false;
                player.setGameMode(GameType.SURVIVAL);
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.getAbilities().invulnerable = true;
                player.onUpdateAbilities();
                player.inventoryMenu.broadcastChanges();
                clientTicks = 0;
                phase = 1;
            } else if (phase == 3 && ++serverTicks >= 6) {
                ItemStack held = player.getItemInHand(test.hand());
                charged = crossbow() && CrossbowItem.isCharged(held);
                if (test.condition().equals("READY") && crossbow()) {
                    require(charged, "Crossbow did not load: " + test + " result=" + useResult + " guard=" + FourWeaponUseGuard.blocksNativeSourceAction(held));
                    clientTicks = 0;
                    phase = 4;
                } else verify(player);
            } else if (phase == 5 && ++serverTicks >= 6) verify(player);
        } catch (Throwable problem) { finish(problem); }
    }

    private void verify(ServerPlayer player) {
        Case test = cases.get(index);
        var projectiles = new ArrayList<Projectile>();
        for (var entity : player.serverLevel().getAllEntities()) {
            if (entity instanceof Projectile projectile && projectile.getOwner() == player && !entity.isRemoved()) projectiles.add(projectile);
        }
        boolean positive = test.condition().equals("READY");
        ItemStack weapon = player.getItemInHand(test.hand());
        int ammunition = player.getInventory().getItem(9).getCount();
        require(projectiles.size() == (positive ? 1 : 0), "Projectile mismatch " + test + " count=" + projectiles.size() + " use=" + useResult);
        require(ammunition == (test.condition().equals("NO_AMMO") ? 0 : positive ? 63 : 64), "Ammunition conservation failed " + test + " ammo=" + ammunition);
        require(positive ? weapon.getDamageValue() > beforeDamage : weapon.getDamageValue() == beforeDamage, "Durability mismatch " + test);
        require(!crossbow() || !CrossbowItem.isCharged(weapon), "Crossbow charged unexpectedly " + test);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("item", test.item());
        result.put("hand", test.hand().name());
        result.put("condition", test.condition());
        result.put("useResult", useResult);
        result.put("loadedBeforeShot", charged);
        result.put("projectiles", projectiles.size());
        result.put("ammunitionRemaining", ammunition);
        result.put("damageBefore", beforeDamage);
        result.put("damageAfter", weapon.getDamageValue());
        result.put("status", "PASS");
        if (positive) result.put("projectileNbtKeys", projectiles.get(0).saveWithoutId(new CompoundTag()).getAllKeys());
        results.add(result);
        System.out.println("[BowLoadProbe] PASS " + test);
        caseTicks = 0;
        if (++index == cases.size()) finish(null); else phase = 0;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || phase < 0) return;
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.gameMode == null) return;
            Case test = cases.get(index);
            if (phase == 1) {
                ItemStack held = client.player.getItemInHand(test.hand());
                if (!test.item().equals(ForgeRegistries.ITEMS.getKey(held.getItem()).toString()) || ++clientTicks < 6) return;
                client.player.setXRot(-35);
                client.player.setYRot(180);
                client.options.keyUse.setDown(true);
                useResult = client.gameMode.useItem(client.player, test.hand()).name();
                clientTicks = 0;
                phase = 2;
            } else if (phase == 2 && ++clientTicks >= (test.condition().equals("UNDERCHARGED") ? 1 : 35)) {
                client.options.keyUse.setDown(false);
                client.gameMode.releaseUsingItem(client.player);
                serverTicks = 0;
                phase = 3;
            } else if (phase == 4 && CrossbowItem.isCharged(client.player.getItemInHand(test.hand())) && ++clientTicks >= 3) {
                client.gameMode.useItem(client.player, test.hand());
                serverTicks = 0;
                phase = 5;
            }
        } catch (Throwable problem) { finish(problem); }
    }

    private void finish(Throwable problem) {
        phase = -1;
        MinecraftForge.EVENT_BUS.unregister(this);
        try {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("status", problem == null ? "PASS_REAL_NETWORK_BOW_CROSSBOW" : "FAIL");
            report.put("cases", results);
            report.put("expectedCases", cases.size());
            if (problem != null) report.put("failure", problem.toString());
            Files.writeString(instance.resolve("evidence/BOW_LOAD.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Throwable writeFailure) { if (problem == null) problem = writeFailure; }
        if (problem == null) completed.run(); else failed.accept(problem);
    }
}
