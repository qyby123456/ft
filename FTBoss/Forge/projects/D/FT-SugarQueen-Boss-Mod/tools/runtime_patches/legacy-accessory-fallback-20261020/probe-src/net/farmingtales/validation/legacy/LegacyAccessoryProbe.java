package net.farmingtales.validation.legacy;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.client.FourWeaponCavityPresentation;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.client.SwordCavityContainerScreen;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 私有测试世界的新旧装配入口、主副手、普通点击与服务端材料守恒回归。 */
@Mod("ftboss_weapon_ui_probe")
public final class LegacyAccessoryProbe {
    private static final List<String> ITEMS = List.of("minecraft:iron_pickaxe", "minecraft:iron_shovel", "minecraft:shears", "minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow");
    private final List<Map<String, Object>> results = new ArrayList<>();
    private volatile int stage;
    private volatile int index;
    private volatile int part;
    private volatile Throwable failure;
    private int serverTicks;
    private int clientTicks;
    private int settle;
    private boolean isolated;
    private boolean closed;
    private BlockPos statue;
    private Object previousHub;
    private Path instance;
    private int checks;
    public LegacyAccessoryProbe() { MinecraftForge.EVENT_BUS.register(this); }
    private InteractionHand hand() { return index % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND; }
    private String item() { return ITEMS.get(index / 2); }
    private void check(boolean result, String label) {
        checks++;
        if (!result) throw new IllegalStateException(label + " index=" + index + " stage=" + stage);
    }
    private static Object field(Object owner, String name) throws Exception {
        var field = StatueHubScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }
    private static Object invoke(Object owner, String name) throws Exception {
        var method = StatueHubScreen.class.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(owner);
    }
    @SubscribeEvent
    public void server(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || stage == 30 || failure != null) return;
        try {
            var players = event.getServer().getPlayerList().getPlayers();
            if (players.isEmpty() || ++serverTicks < 60) return;
            if (serverTicks > 12000) throw new IllegalStateException("Server timeout");
            var player = players.get(0);
            if (!FourWeaponTransactionNetwork.negotiated(player)) return;
            if (stage == 0) {
                if (index == ITEMS.size() * 2) { stage = 30; return; }
                if (instance == null) {
                    instance = Path.of(System.getProperty("weapon.ui.probe.instance")).toRealPath();
                    check(event.getServer().getWorldPath(LevelResource.ROOT).toRealPath().equals(instance.resolve("saves/native_wear_client_smoke").toRealPath()), "Private world guard");
                    player.teleportTo(player.getX(), 180, player.getZ());
                    player.serverLevel().setBlock(player.blockPosition().below(), Blocks.STONE.defaultBlockState(), 3);
                    statue = player.blockPosition().offset(2, 0, 0);
                    var block = ForgeRegistries.BLOCKS.getValues().stream().filter(value -> value instanceof CandyStatueBlock).findFirst().orElseThrow();
                    player.serverLevel().setBlock(statue.below(), Blocks.STONE.defaultBlockState(), 3);
                    player.serverLevel().setBlock(statue, block.defaultBlockState().setValue(CandyStatueBlock.HALF, DoubleBlockHalf.LOWER), 2);
                    player.serverLevel().setBlock(statue.above(), block.defaultBlockState().setValue(CandyStatueBlock.HALF, DoubleBlockHalf.UPPER), 3);
                }
                player.closeContainer();
                player.getInventory().clearContent();
                player.getInventory().selected = 0;
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                var weapon = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item())));
                weapon.getOrCreateTag().putString("LegacyProbeMarker", "preserve");
                player.setItemInHand(hand(), weapon);
                player.getInventory().setItem(9, new ItemStack(Items.IRON_INGOT, 16));
                player.setGameMode(GameType.SURVIVAL);
                player.getAbilities().invulnerable = true;
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                player.setAirSupply(player.getMaxAirSupply());
                player.setHealth(player.getMaxHealth());
                check(FoodWeaponEvolution.isEvolutionCandidate(weapon) || FourWeaponCavityPresentation.usesCavity(weapon), "Test weapon eligible");
                check(FourWeaponCavityPresentation.usesCavity(weapon) == (index >= 6), "Expected routing family");
                part = 0;
                settle = 0;
                stage = 1;
                StatueMenuNetwork.open(player, statue);
                System.out.println("[LegacyAccessoryProbe] PREPARE " + item() + " " + hand());
            } else if (stage == 3) {
                var weapon = player.getItemInHand(hand());
                if (WeaponMaterialBuild.materials(weapon).get(part) != FoodWeaponBuildRules.Material.IRON) return;
                check(player.getInventory().getItem(9).getCount() == 15 - part, "Exactly one consumed");
                check("preserve".equals(weapon.getTag().getString("LegacyProbeMarker")), "Unrelated NBT preserved");
                check(WeaponMaterialBuild.materials(ItemStack.of(weapon.save(new CompoundTag()))).get(part) == FoodWeaponBuildRules.Material.IRON, "Material survives NBT reload");
                var before = weapon.save(new CompoundTag());
                check(!WeaponMaterialBuild.replaceFromUi(player, part * 36 + 9), "Repeated material rejected");
                check(!WeaponMaterialBuild.replaceFromUi(player, -1) && !WeaponMaterialBuild.replaceFromUi(player, 108), "Invalid requests rejected");
                check(before.equals(weapon.save(new CompoundTag())) && player.getInventory().getItem(9).getCount() == 15 - part, "Rejected request does not mutate");
                stage = 4;
            } else if (stage == 11) {
                var weapon = player.getItemInHand(hand());
                var before = weapon.save(new CompoundTag());
                check(!WeaponMaterialBuild.replaceFromUi(player, 9), "New weapon rejects old insertion");
                check(before.equals(weapon.save(new CompoundTag())) && player.getInventory().getItem(9).getCount() == 16, "New weapon rejection preserves material and NBT");
                stage = 12;
            }
        } catch (Throwable problem) { failure = problem; }
    }
    private void click(StatueHubScreen screen, int horizontal, int vertical) {
        check(!net.minecraft.client.gui.screens.Screen.hasShiftDown(), "No Shift input");
        screen.mouseClicked(horizontal, vertical, 0);
        screen.mouseReleased(horizontal, vertical, 0);
    }
    private void next() {
        results.add(Map.of("item", item(), "hand", hand().name(), "entry", index % 3, "ui", index < 6 ? "legacy_three_parts" : "new_container", "installedParts", index < 6 ? 3 : 0));
        index++;
        stage = 0;
        settle = 0;
    }
    @SubscribeEvent
    public void client(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || closed) return;
        var client = Minecraft.getInstance();
        try {
            if (!isolated) {
                MinecraftForge.EVENT_BUS.unregister(Class.forName("dev.codex.sdmshopjei.SdmShopJeiAddon$ClientEvents"));
                isolated = true;
            }
            if (++clientTicks > 18000) throw new IllegalStateException("Client timeout at " + stage);
            if (failure != null || stage == 30) { finish(client); return; }
            if (client.player == null || client.screen == null || index >= ITEMS.size() * 2) return;
            if (stage == 1 && client.screen instanceof StatueHubScreen screen && screen != previousHub
                    && item().equals(ForgeRegistries.ITEMS.getKey(client.player.getItemInHand(hand()).getItem()).toString()) && ++settle > 20) {
                previousHub = screen;
                check(!(Boolean) field(screen, "actionPending"), "Entry not pending");
                var pendingField = StatueHubScreen.class.getDeclaredField("actionPending");
                pendingField.setAccessible(true);
                var initialSection = field(screen, "section");
                pendingField.setBoolean(screen, true);
                invoke(screen, "openBuildAdjustment");
                invoke(screen, "openSwordCavity");
                check(field(screen, "section") == initialSection && client.screen == screen, "Pending request blocks both entry points");
                pendingField.setBoolean(screen, false);
                if (index % 3 == 0) invoke(screen, "openBuildAdjustment");
                else if (index % 3 == 1) invoke(screen, "openSwordCavity");
                else {
                    var section = java.util.Arrays.stream(Class.forName("net.farmingtales.ftboss.client.StatueHubScreen$Section").getEnumConstants()).filter(value -> value.toString().equals("MATERIAL")).findFirst().orElseThrow();
                    int ordinal = ((Enum<?>) section).ordinal();
                    click(screen, (Integer) field(screen, "left") + 30, (Integer) field(screen, "top") + 50 + ordinal * 30 + 10);
                }
                stage = index < 6 ? 2 : 10;
                settle = 0;
            } else if (stage == 2 && client.screen instanceof StatueHubScreen screen && ++settle > 12) {
                check(field(screen, "section").toString().equals("MATERIAL"), "Old material section");
                check(((Button) field(screen, "replaceButton")).visible, "Old confirmation visible");
                int horizontal = (Integer) invoke(screen, "contentX");
                int vertical = (Integer) invoke(screen, "contentY");
                int width = (Integer) invoke(screen, "contentWidth");
                click(screen, horizontal + part * (width / 3) + 8, vertical + 110);
                click(screen, horizontal + 8, vertical + 166);
                var button = (Button) field(screen, "replaceButton");
                check(button.active && button.visible, "Normal-click insertion enabled");
                if (part == 0) Screenshot.grab(client.gameDirectory, "legacy-" + index + ".png", client.getMainRenderTarget(), message -> {});
                click(screen, button.getX() + 8, button.getY() + 8);
                check((Boolean) field(screen, "actionPending"), "Real insertion network request pending");
                stage = 3;
                settle = 0;
            } else if (stage == 4 && client.screen instanceof StatueHubScreen screen && !(Boolean) field(screen, "actionPending")
                    && client.player.getInventory().getItem(9).getCount() == 15 - part && ++settle > 5) {
                part++;
                settle = 0;
                if (part == 3) next(); else stage = 2;
            } else if (stage == 10 && client.screen instanceof SwordCavityContainerScreen screen && ++settle > 20) {
                check(item().equals(ForgeRegistries.ITEMS.getKey(screen.getMenu().weapon().getItem()).toString()), "New menu keeps held weapon");
                Screenshot.grab(client.gameDirectory, "new-" + index + ".png", client.getMainRenderTarget(), message -> {});
                stage = 11;
            } else if (stage == 12) {
                check(client.screen instanceof SwordCavityContainerScreen, "New menu remains open");
                next();
            }
        } catch (Throwable problem) { failure = problem; }
    }
    private void finish(Minecraft client) throws Exception {
        closed = true;
        var report = Map.of("status", failure == null ? "PASS" : "FAIL", "failure", failure == null ? "" : failure.toString(), "checks", checks, "cases", results, "stage", stage, "index", index);
        if (failure != null) failure.printStackTrace();
        Files.writeString(Path.of(System.getProperty("weapon.ui.probe.report")), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("[LegacyAccessoryProbe] " + report);
        client.stop();
    }
}
