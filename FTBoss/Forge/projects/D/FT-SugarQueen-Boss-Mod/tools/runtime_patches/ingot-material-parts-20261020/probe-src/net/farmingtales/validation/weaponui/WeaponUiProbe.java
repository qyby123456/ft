package net.farmingtales.validation.weaponui;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.client.SwordCavityContainerScreen;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.minecraft.nbt.CompoundTag;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 隔离世界中的主副手鼠标配件回归；检查数量守恒、重载、返回菜单与有限开界面重试。 */
@Mod("ftboss_weapon_ui_probe")
public final class WeaponUiProbe {
    private static final List<String> ITEMS = items();
    private static List<String> items() {
        var items = new ArrayList<String>();
        items.add("irons_spellbooks:graybeard_staff");
        items.add("minecraft:iron_sword");
        for (String item : FourWeaponFoundationProfiles.examples().keySet()) if (!items.contains(item)) items.add(item);
        return List.copyOf(items);
    }
    private static int totalCases() { return Integer.getInteger("weapon.ui.probe.cases", 18); }
    private String currentItem() { return List.of("minecraft:trident", "irons_spellbooks:graybeard_staff", "minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "irons_spellbooks:artificer_cane", "irons_spellbooks:ice_staff", "minecraft:iron_axe", "ftboss:qizai_prayer_staff").get(index / 2); }
    private String currentOffering() { return List.of("cataclysm:ancient_metal_nugget", "irons_spellbooks:mithril_weave", "irons_spellbooks:raw_mithril", "cataclysm:burning_ashes", "cataclysm:black_steel_nugget", "irons_spellbooks:pyrium_ingot", "irons_spellbooks:lightning_bottle", "cataclysm:monstrous_horn", "irons_spellbooks:divine_soulshard").get(index / 2); }
    private InteractionHand hand() { return index % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND; }
    private final List<Map<String, Object>> serverChecks = new ArrayList<>();
    private final List<Map<String, Object>> results = new ArrayList<>();
    private volatile int stage;
    private volatile int index;
    private volatile boolean finished;
    private volatile Throwable failure;
    private boolean closed;
    private int serverTicks;
    private int rendered;
    private long renderStart;
    private final List<Double> frameTimes = new ArrayList<>();
    private BlockPos statue;
    private Path instance;
    private Object previousHub;
    private long deadlineMillis;
    private boolean shopIsolated;
    private volatile int targetSlot;
    private volatile int initialRevision;
    private volatile long clickStarted;
    private final List<Double> acknowledgements = new ArrayList<>();
    private int hubWait;
    private int openRetryTicks;
    private int openRetries;
    private Object replyTap;
    private Object snapshotTap;
    private String observedClient = "";
    private String observedServer = "";
    private Object previousPresentation;
    private CompoundTag previousPresentationTag;
    private int presentationRefreshes;
    private Runnable releasePointer;
    private int releaseTicks;
    private boolean tridentStarted;

    private static Object get(Object owner, Class<?> type, String name) throws Exception {
        var field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    private static String state() {
        try {
            var transactions = get(null, FourWeaponClientApi.class, "transactions");
            if (transactions == null) return "transactions=null";
            StringBuilder state = new StringBuilder();
            for (String name : List.of("opening", "pending", "authority", "sourceChanged", "deliveryUncertain", "acceptedRequest", "snapshotConfirmed", "confirmedContainer", "queueContainer")) {
                state.append(name).append('=').append(get(transactions, transactions.getClass(), name)).append(';');
            }
            return state.toString();
        } catch (Exception failure) { return failure.toString(); }
    }

    @SuppressWarnings("unchecked")
    private void taps() throws Exception {
        Class<?> type = net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork.class;
        var field = type.getDeclaredField("client");
        field.setAccessible(true);
        var current = field.get(null);
        if (current != null && current != replyTap) {
            var previous = (java.util.function.Consumer<net.farmingtales.ftboss.transaction.FourWeaponTransactions.Reply>) current;
            java.util.function.Consumer<net.farmingtales.ftboss.transaction.FourWeaponTransactions.Reply> tap = reply -> {
                System.out.println("[WeaponUiProbe] REPLY " + reply + " before=" + state());
                previous.accept(reply);
                System.out.println("[WeaponUiProbe] REPLY_AFTER " + state());
            };
            replyTap = tap;
            field.set(null, tap);
        }
    }

    public WeaponUiProbe() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || finished) return;
        try {
            var players = event.getServer().getPlayerList().getPlayers();
            if (players.isEmpty() || ++serverTicks < 60) return;
            if (serverTicks > 30000) throw new IllegalStateException("UI probe timed out at stage=" + stage + " item=" + currentItem());
            ServerPlayer player = players.get(0);
            if (stage == 0 && !net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork.negotiated(player)) return;
            if (stage == 1 || stage == 2) {
                String observed = player.getItemInHand(hand()).save(new CompoundTag()).toString();
                if (!observed.equals(observedServer)) {
                    observedServer = observed;
                    System.out.println("[WeaponUiProbe] SERVER_NBT stage=" + stage + " " + observed);
                }
            }
            if (stage == 0) {
                instance = Path.of(System.getProperty("weapon.ui.probe.instance")).toRealPath();
                deadlineMillis = System.currentTimeMillis() + 1800000;
                Path actual = event.getServer().getWorldPath(LevelResource.ROOT).toRealPath();
                if (!actual.equals(instance.resolve("saves/native_wear_client_smoke").toRealPath())) throw new IllegalStateException("Not the private test world: " + actual);
                statue = player.blockPosition().offset(2, 0, 0);
                var block = ForgeRegistries.BLOCKS.getValues().stream().filter(value -> value instanceof CandyStatueBlock).findFirst().orElseThrow();
                var level = player.serverLevel();
                level.setBlock(statue.below(), Blocks.STONE.defaultBlockState(), 3);
                level.setBlock(statue, block.defaultBlockState().setValue(CandyStatueBlock.HALF, DoubleBlockHalf.LOWER), 2);
                level.setBlock(statue.above(), block.defaultBlockState().setValue(CandyStatueBlock.HALF, DoubleBlockHalf.UPPER), 3);
                prepare(player);
            } else if (stage >= 5 && stage <= 13 && (stage & 1) == 1) {
                var menu = (net.farmingtales.ftboss.menu.SwordCavityMenu) player.containerMenu;
                boolean acknowledged = switch (stage) {
                    case 5 -> menu.getCarried().getCount() == 2 && menu.getCarried().is(ForgeRegistries.ITEMS.getValue(new ResourceLocation(currentOffering())));
                    case 7 -> menu.slots.get(targetSlot).getItem().getCount() == 1 && menu.getCarried().getCount() == 1;
                    case 9 -> menu.getCarried().isEmpty() && player.getInventory().getItem(9).getCount() == 1;
                    case 11 -> menu.slots.get(targetSlot).getItem().isEmpty() && menu.getCarried().getCount() == 1;
                    case 13 -> menu.getCarried().isEmpty() && player.getInventory().getItem(9).getCount() == 2;
                    default -> false;
                };
                if (acknowledged) {
                    if ((stage == 9 || stage == 13) && !MaterialBonusProbe.equipped(player, menu.weapon(), instance)) {
                        if (System.nanoTime() - clickStarted > 15000000000L) throw new IllegalStateException("Equipped attributes did not update stage=" + stage);
                        return;
                    }
                    acknowledgements.add((System.nanoTime() - clickStarted) / 1000000.0);
                    if (stage == 5) {
                        targetSlot = -1;
                        for (int slot = 0; slot < 27; slot++) {
                            if (!menu.reference(slot) && menu.slots.get(slot).mayPlace(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(currentOffering()))))) { targetSlot = slot; break; }
                        }
                        if (targetSlot < 0) throw new IllegalStateException("No editable material cell " + currentItem());
                        initialRevision = net.farmingtales.ftboss.menu.WeaponCavityMenuRules.revision(menu.weapon());
                    } else if (stage == 7 || stage == 11) {
                        int revision = net.farmingtales.ftboss.menu.WeaponCavityMenuRules.revision(menu.weapon());
                        if (revision <= initialRevision) throw new IllegalStateException("Material edit did not advance revision");
                        initialRevision = revision;
                        var loaded = ItemStack.of(menu.weapon().save(new CompoundTag()));
                        if (net.farmingtales.ftboss.menu.WeaponCavityMenuRules.containerItems(loaded).get(targetSlot).getCount()
                                != (stage == 7 ? 1 : 0)) throw new IllegalStateException("Material edit did not survive NBT reload");
                    }
                    stage++;
                } else if (System.nanoTime() - clickStarted > 15000000000L) {
                    System.out.println("[MaterialDiagnostic] weapon=" + menu.weapon().save(new CompoundTag()) + " view=" + FourWeaponFoundationAdapter.read(menu.weapon()).failure() + " cursor=" + menu.getCarried() + " cell=" + menu.slots.get(targetSlot).getItem() + " mayPlace=" + menu.slots.get(targetSlot).mayPlace(menu.getCarried()) + " policy=" + new net.farmingtales.ftboss.transaction.FoundationApprovedRules().fingerprint(player, menu.weapon()));
                    throw new IllegalStateException("Interaction ack timeout stage=" + stage);
                }
            } else if (stage >= 15 && stage <= 21 && (stage & 1) == 1) {
                var menu = (net.farmingtales.ftboss.menu.SwordCavityMenu) player.containerMenu;
                boolean acknowledged = switch (stage) {
                    case 15 -> menu.getCarried().getCount() == 1 && menu.slots.get(targetSlot).getItem().getCount() == 1;
                    case 17 -> menu.getCarried().isEmpty() && player.getInventory().getItem(9).getCount() == 1;
                    case 19 -> menu.getCarried().getCount() == 1 && menu.slots.get(targetSlot).getItem().isEmpty();
                    case 21 -> menu.getCarried().isEmpty() && player.getInventory().getItem(9).getCount() == 2;
                    default -> false;
                };
                if (acknowledged) {
                    if (stage == 15 || stage == 19) {
                        var loaded = ItemStack.of(menu.weapon().save(new CompoundTag()));
                        if (net.farmingtales.ftboss.menu.WeaponCavityMenuRules.containerItems(loaded).get(targetSlot).getCount()
                                != (stage == 15 ? 1 : 0)) throw new IllegalStateException("Drag/right-click edit not persistent");
                    }
                    stage++;
                } else if (System.nanoTime() - clickStarted > 15000000000L) {
                    throw new IllegalStateException("Drag/right-click timeout stage=" + stage + " cursor=" + menu.getCarried()
                            + " cell=" + menu.slots.get(targetSlot).getItem());
                }
            } else if (stage == 1 && ++hubWait == 300) {
                StatueMenuNetwork.open(player, statue);
            } else if (stage == 4) {
                if (++index == totalCases()) {
                    stage = 30;
                } else {
                    prepare(player);
                }
            }
        } catch (Throwable problem) {
            failure = problem;
            finished = true;
        }
    }

    private void prepare(ServerPlayer player) {
        if (index == 0) { RuleCacheProbe.run(instance); EnvironmentMaterialProbe.run(instance, player, statue); }
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(statue.getX() - 1.0, statue.getY(), statue.getZ() + 0.5);
        player.setAirSupply(300);
        player.setHealth(player.getMaxHealth());
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(currentItem()));
        if (item == null) throw new IllegalStateException("Missing test item " + currentItem());
        player.closeContainer();
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(hand(), new ItemStack(item));
        player.getInventory().setItem(9, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(currentOffering())), 2));
        ItemStack held = player.getItemInHand(hand());
        var checks = new LinkedHashMap<String, Object>();
        checks.put("item", currentItem());
        checks.put("hand", hand().name());
        checks.put("maxDamage", held.getMaxDamage());
        if (FourWeaponFoundationAdapter.applies(held)) {
            var before = held.save(new CompoundTag());
            if (FoundationEntryPoints.seedFromStatue(player, held, statue, false) || !before.equals(held.save(new CompoundTag()))) throw new IllegalStateException("Unnegotiated seed mutated weapon");
            if (FoundationEntryPoints.seedFromStatue(player, held.copy(), statue, true) || !before.equals(held.save(new CompoundTag()))) throw new IllegalStateException("Detached seed accepted");
            if (FoundationEntryPoints.seedFromStatue(player, held, statue.offset(0, 0, 20), true) || !before.equals(held.save(new CompoundTag()))) throw new IllegalStateException("Distant invalid statue seed accepted");
            held.setCount(2);
            var stacked = held.save(new CompoundTag());
            if (FoundationEntryPoints.seedFromStatue(player, held, statue, true) || !stacked.equals(held.save(new CompoundTag()))) throw new IllegalStateException("Stacked weapon accepted");
            held.setCount(1);
            if (held.getMaxDamage() == 0) {
                held.getOrCreateTagElement(FoodWeaponLifecycle.ROOT).putString("Weapon", "invalid");
                var malformed = held.save(new CompoundTag());
                if (FoundationEntryPoints.seedFromStatue(player, held, statue, true) || !malformed.equals(held.save(new CompoundTag()))) throw new IllegalStateException("Malformed identity overwritten");
                held.removeTagKey(FoodWeaponLifecycle.ROOT);
            }
            checks.put("negativeEntryGuards", "PASS");
        }
        if (FoodWeaponSkillLoadout.canBind(player.getItemInHand(hand())) && !FoodWeaponSkillLoadout.bindFromUi(player, 1)) {
            throw new IllegalStateException("Real statue binding failed " + currentItem());
        }
        var fixtureProgress = held.getOrCreateTagElement("FTBossFoodEvolution");
        fixtureProgress.putInt("Level", 4);
        fixtureProgress.putInt("ProgressionVersion", 2);
        fixtureProgress.putInt("Experience", 0);
        rendered = 0;
        previousPresentation = null;
        previousPresentationTag = null;
        presentationRefreshes = 0;
        hubWait = 0;
        openRetryTicks = 0;
        openRetries = 0;
        acknowledgements.clear();
        stage = 1;
        StatueMenuNetwork.open(player, statue);
        var identity = FoodWeaponLifecycle.weaponId(held);
        if (identity == null) throw new IllegalStateException("No UI identity after valid statue open " + currentItem());
        if (!identity.equals(FoodWeaponLifecycle.weaponId(ItemStack.of(held.save(new CompoundTag()))))) throw new IllegalStateException("UI identity did not survive NBT reload");
        if (FourWeaponFoundationAdapter.applies(held) && !FoundationEntryPoints.seedFromStatue(player, held, statue, true)) throw new IllegalStateException("Reopen seed rejected");
        if (!identity.equals(FoodWeaponLifecycle.weaponId(held))) throw new IllegalStateException("Reopen rotated identity");
        if (held.getMaxDamage() == 0) {
            var life = held.getTagElement(FoodWeaponLifecycle.ROOT);
            if (NativeFoodWeaponWear.eligible(held) || FoodWeaponLifecycle.managed(held) || held.getMaxDamage() != 0 || held.getDamageValue() != 0
                    || life.contains("WearStep") || life.contains("RepairProfile") || life.contains("WasEvolved")) throw new IllegalStateException("Menu identity changed native wear eligibility");
            checks.put("nativeWearStillExcluded", true);
        }
        checks.put("identity", identity.toString());
        checks.put("nbtReloadAndReopen", "PASS");
        serverChecks.add(checks);
        System.out.println("[WeaponUiProbe] SERVER_STATE item=" + currentItem() + " id=" + FoodWeaponLifecycle.weaponId(player.getItemInHand(hand()))
                + " foundation=" + (FourWeaponFoundationAdapter.applies(player.getItemInHand(hand())) && FourWeaponFoundationAdapter.read(player.getItemInHand(hand())).valid()) + " pos=" + player.position());
        System.out.println("[WeaponUiProbe] OPEN_HUB " + currentItem());
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        try {
            if (FourWeaponClientApi.capable()) taps();
            if (stage == 1 || stage == 2) {
                String observed = state();
                if (!observed.equals(observedClient)) {
                    observedClient = observed;
                    System.out.println("[WeaponUiProbe] CLIENT_TX stage=" + stage + " " + observed);
                }
            }
            if (!shopIsolated) {
                MinecraftForge.EVENT_BUS.unregister(Class.forName("dev.codex.sdmshopjei.SdmShopJeiAddon$ClientEvents"));
                shopIsolated = true;
            }
            if (releaseTicks > 0) {
                if (--releaseTicks == 0) releasePointer.run();
                return;
            }
            if (stage == 30 && !tridentStarted) {
                tridentStarted = true;
                BowLoadProbe.start(client, instance, statue, () -> finished = true, problem -> { failure = problem; finished = true; });
                return;
            }
            if (!finished && deadlineMillis > 0 && System.currentTimeMillis() > deadlineMillis) {
                throw new IllegalStateException("Wall-clock UI timeout at stage=" + stage + " item=" + currentItem());
            }
            if (finished) {
                if (!closed) finish(client);
                return;
            }
            if (stage == 1 && client.screen instanceof StatueHubScreen && client.screen != previousHub
                    && client.player != null && currentItem().equals(ForgeRegistries.ITEMS.getKey(client.player.getItemInHand(hand()).getItem()).toString())) {
                var action = StatueHubScreen.class.getDeclaredMethod("openSwordCavity");
                System.out.println("[WeaponUiProbe] CLIENT_STATE item=" + currentItem() + " capable=" + FourWeaponClientApi.capable()
                        + " id=" + FoodWeaponLifecycle.weaponId(client.player.getItemInHand(hand()))
                        + " foundation=" + (FourWeaponFoundationAdapter.applies(client.player.getItemInHand(hand())) && FourWeaponFoundationAdapter.read(client.player.getItemInHand(hand())).valid()) + " pos=" + client.player.position());
                action.setAccessible(true);
                stage = 2;
                action.invoke(client.screen);
            } else if (stage == 2 && client.screen instanceof StatueHubScreen && ++openRetryTicks >= 40) {
                openRetryTicks = 0;
                Object transactions = get(null, FourWeaponClientApi.class, "transactions");
                if (transactions != null && get(transactions, transactions.getClass(), "pending") == null && get(transactions, transactions.getClass(), "opening") == null) {
                    if (++openRetries > 3) throw new IllegalStateException("Container open denied after bounded normal UI retries " + currentItem());
                    var action = StatueHubScreen.class.getDeclaredMethod("openSwordCavity");
                    action.setAccessible(true);
                    action.invoke(client.screen);
                    System.out.println("[WeaponUiProbe] OPEN_RETRY " + currentItem() + " count=" + openRetries);
                }
            } else if ((stage == 6 || stage == 8 || stage == 10 || stage == 12) && client.screen instanceof SwordCavityContainerScreen screen) {
                var menu = screen.getMenu();
                if (stage == 6 && menu.getCarried().getCount() != 2 || stage == 8 && (menu.getCarried().getCount() != 1 || menu.slots.get(targetSlot).getItem().getCount() != 1)
                        || stage == 10 && !menu.getCarried().isEmpty() || stage == 12 && (menu.getCarried().getCount() != 1 || !menu.slots.get(targetSlot).getItem().isEmpty())) return;
                int slot = stage == 6 || stage == 10 ? targetSlot : 27;
                stage++;
                clickSlot(screen, slot);
            } else if (stage == 14 && client.screen instanceof SwordCavityContainerScreen screen) {
                if (!screen.getMenu().getCarried().isEmpty() || screen.getMenu().slots.get(27).getItem().getCount() != 2) return;
                results.get(results.size() - 1).put("normalClickAckMs", List.copyOf(acknowledgements));
                results.get(results.size() - 1).put("placeTakeReloadAndConservation", "PASS");
                if (FourWeaponFoundationAdapter.applies(screen.getMenu().weapon())) {
                    stage = 15;
                    dragSlot(screen, 27, targetSlot);
                } else stage = 22;
            } else if ((stage == 16 || stage == 18 || stage == 20) && client.screen instanceof SwordCavityContainerScreen screen) {
                var menu = screen.getMenu();
                if (stage == 16 && menu.getCarried().getCount() != 1 || stage == 18 && !menu.getCarried().isEmpty()
                        || stage == 20 && (menu.getCarried().getCount() != 1 || !menu.slots.get(targetSlot).getItem().isEmpty())) return;
                int slot = stage == 18 ? targetSlot : 27;
                int button = stage == 18 ? 1 : 0;
                stage++;
                clickSlot(screen, slot, button);
            } else if (stage == 22 && client.screen instanceof SwordCavityContainerScreen screen) {
                if (!screen.getMenu().getCarried().isEmpty() || screen.getMenu().slots.get(27).getItem().getCount() != 2) return;
                results.get(results.size() - 1).put("dragAndRightClick", FourWeaponFoundationAdapter.applies(screen.getMenu().weapon()) ? "PASS" : "LEGACY_UNCHANGED");
                var back = screen.children().stream().filter(value -> value instanceof Button button && button.getWidth() == 29).map(value -> (Button) value).findFirst().orElseThrow();
                stage = 3;
                if (!screen.mouseClicked(back.getX() + 2, back.getY() + 2, 0)) throw new IllegalStateException("Back button rejected click");
            } else if (stage == 3 && client.screen instanceof StatueHubScreen) {
                previousHub = client.screen;
                results.get(results.size() - 1).put("returnedToHub", true);
                System.out.println("[WeaponUiProbe] RETURN_HUB " + currentItem());
                stage = 4;
            }
        } catch (Throwable problem) {
            failure = problem;
            finished = true;
        }
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) { renderStart = System.nanoTime(); return; }
        if (event.phase != TickEvent.Phase.END || stage != 2 || finished) return;
        if (Minecraft.getInstance().screen instanceof SwordCavityContainerScreen && renderStart > 0) frameTimes.add((System.nanoTime() - renderStart) / 1000000.0);
        Minecraft client = Minecraft.getInstance();
        try {
            if (client.screen instanceof SwordCavityContainerScreen current) {
                var presentation = net.farmingtales.ftboss.client.CavityPresentationCache.frame(current, current.getMenu().weapon(), client.level);
                var tag = current.getMenu().weapon().serializeNBT();
                if (previousPresentationTag != null && previousPresentationTag.equals(tag) && previousPresentation != presentation) {
                    throw new IllegalStateException("Unchanged material state recomputed completeness on another frame");
                }
                if (previousPresentation != presentation) presentationRefreshes++;
                previousPresentation = presentation;
                previousPresentationTag = tag;
            }
            if (!(client.screen instanceof SwordCavityContainerScreen screen) || ++rendered < 64) return;
            var readiness = SwordCavityContainerScreen.class.getDeclaredMethod("foundationReady");
            readiness.setAccessible(true);
            if (FourWeaponFoundationAdapter.applies(screen.getMenu().weapon()) && !(Boolean) readiness.invoke(screen)) {
                if (rendered > 240) throw new IllegalStateException("Container never input-ready: authority=" + FourWeaponClientApi.authority()
                        + " snapshot=" + (FourWeaponClientApi.snapshot() == null ? "null" : FourWeaponClientApi.snapshot().data()) + " container=" + screen.getMenu().containerId);
                return;
            }
            String image = String.format("%02d-%s.png", index + 1, (currentItem() + "-" + hand().name()).replace(':', '-'));
            Screenshot.grab(client.gameDirectory, image, client.getMainRenderTarget(), message -> System.out.println("[WeaponUiProbe] " + message.getString()));
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("offering", currentOffering());
            result.put("item", currentItem());
            result.put("openRetries", openRetries);
            result.put("hand", hand().name());
            result.put("identity", String.valueOf(FoodWeaponLifecycle.weaponId(screen.getMenu().weapon())));
            String menuItem = ForgeRegistries.ITEMS.getKey(screen.getMenu().weapon().getItem()).toString();
            result.put("menuItem", menuItem);
            if (!currentItem().equals(menuItem)) throw new IllegalStateException("Wrong weapon in actual menu " + menuItem);
            result.put("screen", screen.getClass().getName());
            result.put("renderedFrames", rendered);
            result.put("completenessRefreshes", presentationRefreshes);
            result.put("unchangedFrameReuse", "PASS");
            result.put("frameTimesMs", List.copyOf(frameTimes));
            frameTimes.clear();
            result.put("screenshot", instance.resolve("screenshots").resolve(image).toString());
            result.put("returnedToHub", false);
            results.add(result);
            stage = 5;
            clickSlot(screen, 27);
            System.out.println("[WeaponUiProbe] RENDER_PASS " + currentItem());
        } catch (Throwable problem) {
            failure = problem;
            finished = true;
        }
    }

    private void finish(Minecraft client) throws Exception {
        closed = true;
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", failure == null && results.size() == totalCases() ? "PASS_REAL_CLIENT_UI_SWITCH_AND_RETURN" : "FAIL");
        report.put("cases", results);
        report.put("expectedCases", totalCases());
        report.put("serverChecks", serverChecks);
        report.put("candidateSha256", System.getProperty("weapon.ui.probe.sha256"));
        if (failure != null) {
            if (client.screen != null) report.put("screenAtFailure", client.screen.getClass().getName());
            if (client.player != null) report.put("heldAtFailure", client.player.getItemInHand(hand()).toString());
            var text = new java.io.StringWriter();
            failure.printStackTrace(new java.io.PrintWriter(text));
            report.put("failure", text.toString());
            report.put("unfinishedFrameTimesMs", List.copyOf(frameTimes));
            report.put("transactionState", state());
        }
        Files.writeString(Path.of(System.getProperty("weapon.ui.probe.report")), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("[WeaponUiProbe] " + report.get("status"));
        client.stop();
    }

    private void nativeMouse(double horizontal, double vertical, int button, int action) {
        try {
            Minecraft client = Minecraft.getInstance();
            Class<?>[] signature = action < 0 ? new Class<?>[]{long.class, double.class, double.class}
                    : new Class<?>[]{long.class, int.class, int.class, int.class};
            var method = java.util.Arrays.stream(net.minecraft.client.MouseHandler.class.getDeclaredMethods())
                    .filter(candidate -> !candidate.isSynthetic() && java.util.Arrays.equals(candidate.getParameterTypes(), signature)
                            && (signature.length == 3 ? java.util.Set.of("onMove", "m_91561_") : java.util.Set.of("onPress", "m_91530_")).contains(candidate.getName()))
                    .findFirst().orElseThrow();
            method.setAccessible(true);
            if (action < 0) method.invoke(client.mouseHandler, client.getWindow().getWindow(),
                    horizontal * client.getWindow().getScreenWidth() / client.getWindow().getGuiScaledWidth(),
                    vertical * client.getWindow().getScreenHeight() / client.getWindow().getGuiScaledHeight());
            else method.invoke(client.mouseHandler, client.getWindow().getWindow(), button, action, 0);
        } catch (ReflectiveOperationException problem) { throw new IllegalStateException(problem); }
    }

    private void clickSlot(SwordCavityContainerScreen screen, int index) { clickSlot(screen, index, 0); }

    private void clickSlot(SwordCavityContainerScreen screen, int index, int button) {
        var slot = screen.getMenu().slots.get(index);
        double horizontal = screen.getGuiLeft() + slot.x + 8;
        double vertical = screen.getGuiTop() + slot.y + 8;
        clickStarted = System.nanoTime();
        nativeMouse(horizontal, vertical, button, -1);
        releasePointer = () -> {
            nativeMouse(horizontal, vertical, button, 1);
            releasePointer = () -> nativeMouse(horizontal, vertical, button, 0);
            releaseTicks = 2;
        };
        releaseTicks = 2;
    }

    private void dragSlot(SwordCavityContainerScreen screen, int source, int target) {
        var from = screen.getMenu().slots.get(source);
        var to = screen.getMenu().slots.get(target);
        double fromX = screen.getGuiLeft() + from.x + 8;
        double fromY = screen.getGuiTop() + from.y + 8;
        double toX = screen.getGuiLeft() + to.x + 8;
        double toY = screen.getGuiTop() + to.y + 8;
        clickStarted = System.nanoTime();
        nativeMouse(fromX, fromY, 0, -1);
        releasePointer = () -> {
            nativeMouse(fromX, fromY, 0, 1);
            nativeMouse(toX, toY, 0, -1);
            releasePointer = () -> nativeMouse(toX, toY, 0, 0);
            releaseTicks = 2;
        };
        releaseTicks = 2;
    }
}
