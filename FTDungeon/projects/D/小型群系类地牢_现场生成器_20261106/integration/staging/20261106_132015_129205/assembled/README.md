# B / runtime — isolated Forge 1.20.1 candidate

Java 17, Forge 47.4.10, modid `ft_biome_dungeon_generator`. This component writes only runtime/. No existing FT mod replacement, automatic worldgen, official instance deployment, or player-world launch. This is an isolated-airspace architectural MVP candidate, not combat/loot progression and not a transactional world editor.

## Build and tests

Run PowerShell with cwd = this runtime directory:

```powershell
.\SNAPSHOT-CATALOG.ps1
.\BUILD.ps1 -Task runtimeSelfTest -Offline
.\BUILD.ps1 -Task runData -Offline
.\BUILD.ps1 -Task runGameTestServer -Offline
.\BUILD.ps1 -Task build -Offline
```

`BUILD.ps1` temporarily selects `$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot` JDK17, overridable by `JAVA_HOME_17`; restores process environment. No tests run in the formal instance. Gradle/cache artifacts are not copied from the reference project. `build.gradle` defaults to real `../core/src/main/java`; integration may supply `-PcoreSource=<frozen actual core source root>`. Production has no core stub. The two accepted, unchanged `s8_05`, `s8_06` room NBT files and manifest are hash-verified snapshots inside `src/main/resources/data/biomedungeon`. Catalog source is not a Java dependency.

`runtimeSelfTest`: real atomic-file, journal frame checksum/truncation/tamper and replay-policy tests; not a world test. `runData`: Forge-initialized real registry and real core/catalog validation, 16 distinct room-level plans, all four rotations, envelope/doors/sealing/headroom and unsafe-resource rejection. `runGameTestServer`: real isolated Forge server in `run/gametest` and two executable test cases using the real runtime. A successful Gradle exit with zero tests is NOT a pass; look for positive required-test count and `RUNTIME_WORLD_PASS` lines. The first attempted launcher omitted the template namespace from enabled namespaces and ran zero tests; retained as failed coverage evidence. GameTest/validation source sets are not included in candidate JAR.

GameTests explicitly authorize their own scratch world with a test-launcher property; production has no marker-creation command. A reused scratch world retains reservations and evidence: use `-GradleOptions @('-PgameTestDir=run/another-fresh-test')` for independent reruns rather than deleting evidence. Source-run Forge GameTests test actual compiled production classes; the reobfuscated candidate JAR is separately built, not claimed to have been installed into that test server. Exact current results and hashes are in ready.json/progress.json.

Separate-process interrupted restart fixture (same isolated world, two independent JVM launches):

```powershell
.\BUILD.ps1 -Task runGameTestServer -Offline -GradleOptions @('-PrestartStage=prepare','-PgameTestDir=run/restart-gametest')
.\BUILD.ps1 -Task runGameTestServer -Offline -GradleOptions @('-PrestartStage=resume','-PgameTestDir=run/restart-gametest')
```

Prepare checkpoints partway through BUILDING and cleanly stops the server. Resume asserts that a new process loads the same durable instance without automatic writes, explicitly resumes, then reads back every cell. This is real separate-process recovery, not force-kill or power-loss certification. Logs include distinct process IDs and the same operation hash. Default full world tests also check actual collision/floor connectivity between all used connector feet and ticket release; these assertions only certify the exercised real seed/layout.

## Commands (permission level >= 2, server only)

All coordinates are absolute block coordinates, same dimension as the command source:

```text
/ftbiomedungeon plan <x> <y> <z> <biome> <rooms> <ordinal>
/ftbiomedungeon build <instanceId> CONFIRM_ISOLATED_AIRSPACE
/ftbiomedungeon pause <instanceId>
/ftbiomedungeon status <instanceId>
/ftbiomedungeon inspect <instanceId>
/ftbiomedungeon list
```

Example on a NEW, isolated Forge test world: `/ftbiomedungeon plan 1000 200 1000 s8 2 0`. It returns the real core plan ID, envelope and operation count; `inspect` exposes complete actual room-level plan JSON. Repeating an identical request reuses its existing durable plan. Plan is always dry-run: it writes only its own durable intent/reservation file, never any world block. `build` requires the returned exact ID and literal confirmation. Build starts full asynchronous preflight, not immediate placement. `pause` preserves blocks and journal; repeating build revalidates before replay. Committed build requests only re-read/verify the same envelope; no second placement or duplicate entities.

Before any plan/build, an operator must manually create `<isolated world root>/ALLOW_ISOLATED_TEST_WORLD` with exact content:

```text
ft_biome_dungeon_generator isolated test only
```

Do not create that marker in a player/production world. It is explicit operator intent, not an automatic proof that the world is disposable. Initial target envelope must be ENTIRELY air, free of all block entities/fluids/entities/players (entities also checked in a two-block halo). Anchor y >= 164, full envelope y >= 160 and below build limit; world border checked. Maximum 8 rooms; envelope interior spans <=192/96/192 and total volume <=1,000,000 cells. Only one active job per dimension. Templates/plan/envelope limits are fail-closed; a core layout exceeding runtime bounds is rejected, not cropped.

Production command gate also refuses any additional loaded mod IDs beyond minecraft/forge/ft_biome_dungeon_generator. Unknown third-party world writers/claims systems therefore do not silently bypass protection in this candidate; modpack integration requires a future reviewed adapter rather than enabling this gate in the formal instance.

## Actual placement and persistence

- Verify raw packaged NBT SHA256 before decoding. No filesystem template path, external datapack resource substitution, or arbitrary namespace. Manifest schema/IDs/dimensions/enumerations/ports/acceptance hash evidence checked. Only native DataVersion 3465. NBT accounting limited to 64 MB, compressed resource <=16 MB.
- Only complete explicit-air single-palette, static vanilla templates in the MVP block allowlist. Unknown blocks/properties, fluids/waterlogging, fire, random-ticking/falling/dynamic blocks, sparse records, ANY entity payload or block-entity NBT are explicitly REJECTED before world writes. The one random-tick flag exception is mature amethyst_cluster: vanilla inherits a random-tick flag but its randomTick implementation is inert; budding_amethyst and buds are not permitted. These unsupported payloads are never silently dropped. General entity/BE placement and restoration are NOT implemented.
- Preserve `PlacedRoom.origin` metadata and frozen eight-corner bounds; use actual core `DungeonGeometry.blockPosition` for unit-cube voxel rotation (q1/q2/q3 one-cell correction) and native BlockState rotation. This avoids spilling block indices outside frozen bounds.
- Links use core EXTERIOR feet, not raw port centers. Dry, orthogonal, equal-height straight/single-bend corridors only; no complex height/wet support. Generated floor/walls/ceiling leave used doorway apertures open. Unused source apertures are sealed in the runtime operation plan. Original packaged room bytes remain unchanged; the assembled room is an adapted architectural candidate requiring world verification.
- Entire envelope (including explicit original air, all corridors, unused-port sealing and outer barrier shell) is reserved and enumerated. Shell is ordered first, then interior. All target chunks acquire nonpersistent region tickets before preflight, released on pause/success/error/unload/stop. No persistent forced chunks are left behind.
- Preflight scans 1024 cells per tick and checks entities every tick. ZERO world writes until all cells pass. Placement processes at most 128 operation cells/tick; verification reads 1024/tick. A later external conflict quarantines partial output instead of overwriting it.
- Before each batch, persist `ownedThrough` high-water intent, then mutate blocks, then persist cursor. Framed compressed-NBT journal has magic, exact length and SHA256; atomic same-directory file replacement with file `force(true)`. Journal includes exact request/plan, world seed/dimension, manifest hash and full assembled-operation hash. Checkpoint failures stop writes. Files are in `<world>/ft_biome_dungeon_generator/<dimension namespace>/<dimension path>/<id>.nbt`; despite extension these files are FTD1 framed NBT, not directly plain NBT. Their first 8 bytes are big-endian magic `0x46544431` and payload length; next64 ASCII SHA256; rest gzip-NBT.
- Restart loads reservations but NEVER automatically resumes world writes. Explicit build regenerates from the real frozen catalog/core plan and compares operation/manifest hashes; full preflight accepts only air or exact expected state within previously attempted prefix. Air permits replay when chunk saves lag journal saves. Unknown blocks/BE/fluids are conflicts. Failed preflight is FAILED; partial-write conflict is ROLLBACK_REQUIRED. Both require manual inspection and cannot auto-resume. No destructive cleanup/rollback command is supplied.
- Read back every final cell and absence of unexpected BEs before commit; save chunk source before durable COMMITTED. Committed replay performs a fresh readback. Break/place/fluid-place/explosion events protect every persisted reservation, including planned/failed/committed jobs. A corrupt journal disables new builds and conservatively refuses edits.

## Safety limits — do not overclaim

This does NOT guarantee ACID transactions or rollback. Original world is required to be air; BE/entity restore is unsupported. A block changed by another actor to the EXACT expected state inside write-ahead-owned intent cannot be distinguished from our own placement. Operator commands, direct mod world edits, piston/tick interactions outside the ordinary subscribed events and third-party claim/protection systems are not fully intercepted. Atomic replace/file force is not proof of hardware/power-loss durability. Crash replay semantics are implemented, but separate-process crash/restart and disk-loss fault injection must be independently tested. Do not deploy to mixed-use worlds. No global gamerules are disabled to hide random ticks or fluid behavior.

The generator currently assembles two normal S8 room types. It does not certify entrance/loot/combat progression. The final exercised layout passes actual level collision/floor BFS between every used connector foot; this does not certify every future seed, player movement mechanic, or a formal entrance/exit progression design. No GameTest result certifies all 8 biomes or second-round 11–20 rooms.

## Reference provenance

Only `gradle/wrapper/gradle-wrapper.jar` and `.properties` were copied from read-only `D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1\gradle\wrapper`. ForgeGradle6.0.54, official1.20.1 mappings, Forge47.4.10 and JDK17 BUILD bootstrap were referenced read-only; BUILD script adapted with local working directory. No existing mod Java/resources/JAR, large caches, configs or saves copied. `catalog-snapshot.json` records exact accepted-template provenance. ready.json hashes every build-consumed source/resource and artifact for integration's read-only snapshot.
