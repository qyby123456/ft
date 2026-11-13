# Multi-style runtime API (B)

## Owned source and integration boundary

B changes only `moss/generator/Gameplay.java`, `MossMod.java`, `MossBossPortals.java`, and public `net/farmingtales/ftdungeonmobs/generator/MossDungeonGenerator.java`. Engine, Planner, Catalog, Store, Profiles, style resources, registry and build are coordinator/A owned. No deployment or formal world/config writes.

Runtime requires coordinator APIs exactly as follows:

```java
Profiles.Style Profiles.get(String styleId);
List<Profiles.Style> Profiles.all();
BlockState Profiles.state(String serializedState);
Engine.Job Engine.plan(ServerLevel level, String style, long seed, BlockPos anchor);
```

`Profiles.Style` has public String `id`, `title`, `icon`, `catalog`, `capBlock`, `barrierBlock`, `melee`, `ranged`, `bossType`, `bossName`; double `bossHealth`, `bossDamage`; long `roomCooldownTicks`; int `instanceCap`. `id` is a canonical resource ID such as `ftmossgen:forest`; `icon` is an item registry ID. Plan and Piece have String `style`. B uses Plan.style as authority and persists its canonical ID in Gameplay.State.style. Engine/Store must validate the frozen profile hash with the frozen Plan before execution, including resume. B does not change Engine cap writes: coordinator must read capBlock and barrierBlock there using Profiles.state. Runtime barrier repair/unlock reads barrierBlock from the owning profile.

## Generator provider

```java
public MossDungeonGenerator();
public MossDungeonGenerator(String style);
```

No-argument constructor selects `ftmossgen:forest`. String constructor resolves and stores canonical `Profiles.get(style).id`. `id()`, `title()`, `icon()` resolve the selected profile. Invalid or AIR item icons fail closed. Coordinator registers one provider per Profiles.all() entry. reserve checks BOTH canonical plan.style and seed before reusing COMPLETE/non-FAILED jobs; new plans call Engine.plan(level, style, seed, anchor). status/start/enter reject instances of another style. All providers own shared `MossMod.DIMENSION` (`ftmossgen:forest`) and use the same safe leave path, with separate instance IDs and Store records.

## Commands

Normal users: `/moss styles`, `/moss status`, `/moss enter <id>`, `/moss leave`.

OP2: `/moss generate <style> <seed>`, `/moss plan <x> <y> <z> <style> <seed>`, `/moss build <id>`, `/moss pause <id>`.

Compatibility: `/moss generate <seed>` and `/moss plan <x> <y> <z> <seed>` select forest. Style arguments use ResourceLocationArgument.id(), so namespaced identifiers do NOT need quotes. Tab suggestions enumerate canonical profile IDs. Generation reuse requires matching style AND seed; mixed-style same-seed jobs are never reused across styles.

## Gameplay state and enemy factory

Existing public initialize/tick/death/startWave/startBoss/gateCells/safeFeet/livingReservations signatures stay unchanged. State adds String `style`. Intent.type persists the chosen configured entity ID at wave/fight preparation, before spawn. Entity type lookup uses ForgeRegistries.ENTITY_TYPES and accepts Minecraft shorthand for existing compatibility; fully qualified IDs are recommended. Public `Gameplay.entityType(String)` validates registry presence. initialize additionally probes factories without adding entities: configured melee/ranged/boss must create Mob, and Boss requires health and attack damage attributes. Invalid profiles fail initialization before runtime state is committed. Large configured entities still pass their actual collision box and entity-occupancy checks before addFreshEntity; unsupported geometry does not get overwritten.

Room cooldown uses profile.roomCooldownTicks; per-instance reservations use profile.instanceCap, retaining five Boss/add reservation slots for room waves. Global cap remains 256 across ALL styles/instances sharing the dimension. First Boss attempt uses profile.bossType, profile.bossName, profile.bossHealth and profile.bossDamage; optional movement attribute preserves default 0.25 behavior. Four adds use owning melee/ranged IDs. Forest merchant remains the existing FT SkeletonMerchantEntity and its original three trades. Default constants remain available for old callers/tests but do not override profile values.

Boss victory requires registered death-event intent role=boss, exact persisted UUID, exact registered Intent.type, FIGHTING state and a real non-FakePlayer ServerPlayer source. No WitherSkeleton instanceof requirement remains. Missing/unloaded entities, generic/nonplayer kills and wrong entity types never grant victory. Reward remains once per instance; cleanup marks only that instance's boss_add intents DEAD. No other style's mobs are touched.

## Native FT internal-gate bridge

Public signatures are unchanged:

```java
public static boolean MossBossPortals.handleUse(ServerPlayer player, BlockPos anchor);
public static boolean MossBossPortals.handleTravel(ServerPlayer player, BlockPos anchor);
public static boolean MossBossPortals.refreshControl(ServerLevel level, BlockPos anchor);
```

`MossBossPortals.ID` stays `ftmossgen:forest` for compatibility; it is NOT the authority for arbitrary gate generator IDs. Native BE.configureGenerator and ownership comparison now use new ResourceLocation(Profiles.get(job.plan.style).id). Native BE DungeonGenerator.Type is therefore the style ID. ForgeData `ftmossgen.portal` records `style` separately from actual `dimension=MossMod.DIMENSION.location()` (always ftmossgen:forest), plus instance, role, anchor, facing and target. The source style never becomes a nonexistent destination dimension. Metadata mismatches fail closed instead of rebinding a foreign style/instance. Existing four FT hooks, 12 native gate parts, no-menu handling, passage intersection, geometry validation, persistent cooldown and safe landing remain intact.

## Frozen data / limits

All style configurations, resource/catalog fingerprints and plan IDs must be frozen/checked by Engine/Store. Changing a profile after planning must be rejected by coordinator's hash validation, not silently applied on resume. Gameplay refuses State.style not matching canonical Plan.style; B does not migrate old worlds or guess missing style identity. Same tag namespace and SavedData name are intentional because style-isolated jobs share one dedicated dimension.

At-most-once spawn/reward persistence behavior is retained: an interrupted ATTEMPTED spawn can remain absent and an interrupted reward can be lost, rather than duplicate mobs/rewards. No automatic missing Boss replacement or inferred victory. Final Forge compilation, profile-resource validation and actual mixed-style server tests are coordinator owned. B performed Java 17 syntax parsing on all four files and inspected Minecraft ResourceLocationArgument and suggestion APIs; this is not a full compilation or gameplay test.

## Production 0.26 closeout: entity preflight limitation

Coordinator reported style-native-r1's three required native tests PASS (nine plans, four JSON profiles, two actual builds, production menu and Boss behavior). This report predates the final message-only change in MossMod from the forest-specific error prefix to `地牢生成器：`.

Current Profiles.validate verifies entity registry presence; it does not instantiate factories or verify Mob inheritance. Gameplay.validateEnemies instantiates configured melee/ranged/boss WITHOUT adding them to the world, rejecting non-Mob/null factories and Bosses without health/attack attributes. However Engine currently calls Gameplay.initialize only in INITIALIZING AFTER all geometry writes. Therefore a registered non-Mob configuration such as minecraft:item or minecraft:armor_stand is rejected too late to promise zero world block writes. No pre-placement non-Mob rejection is claimed by B.

Coordinator recommendation: in server-thread Engine.plan/start preflight, before any placement, probe all three configured factories against ServerLevel and require Mob, plus Boss health/attack attributes. Re-run this check on restored/resumed jobs before the first write. Never add probe entities to the world. Keep profile fingerprint validation and initialization checks as defenses. Engine/Profiles remain coordinator owned; B did not modify them or introduce an unrequested shared-file change for this closeout.
