# Core v1 — replayable room-level planner

Component owner: A. This directory alone was modified. No game instance, source template, runtime, catalog or integration file was modified. This is a **pure Java planning component**, not a Forge mod and not a claim of in-world placement acceptance.

## Consumption

- Production sources: `src/main/java/cn/ft/biomedungeon/core/{DungeonApi,DungeonGeometry,DungeonPlanner}.java`.
- Production-only JAR: `build/ft-biomedungeon-core-v1.jar`; Java class-file major 61; dependency inspection: `java.base` only. No test fixtures or NBT are packaged.
- Frozen API record names, field order and Phase constants match WORK_ORDER v1. `DungeonPlanner.plan(Request,List<Template>,List<Box>)` is public static; an explicit public no-arg constructor also permits legacy instance-call syntax.
- Input and output lists are defensively copied. Templates/ports must have unique IDs. Duplicate template IDs are rejected, never order-dependently deduplicated. Pool, port and occupied-box insertion order do not affect results.
- Runtime/catalog must still establish independent acceptance, actual NBT contents, resource whitelisting and SHA-256 bytes. Core validates metadata shape but does not read JSON/NBT or prove source acceptance.

## Actual algorithm

`ft-core-splitmix64-dfs-v1`: SHA-256 over big-endian, explicitly length-prefixed UTF-8 algorithm ID, worldSeed, dimension, anchor x/y/z, instanceOrdinal, generatorVersion and biome. First eight digest bytes, big-endian signed long, seed an embedded SplitMix64. Fisher-Yates uses unbiased bounded sampling; no JDK random implementation, Object.hashCode or unordered iteration supplies decisions.

Sort templates by ID and ports by ID, then sample root template/rotation and unused outward ports across **all existing nodes**. At every extension, independently shuffle eligible templates, ports, rotations and gaps 3..10. Place a new room by exact opposite-facing aperture alignment, test its bounds, reserve the complete corridor shell, recurse; undo unsuccessful candidates and try alternatives. The result is a connected, bidirectionally traversable geometric tree (N rooms, N-1 links), not a fixed row or rotation of a merged NBT. Template reuse is permitted; this is not a uniqueness sampler. Terminal rooms are leaves. If present, entrance is the root and terminal is the last placed node.

Role aliases: `入口/entrance/entry`; `终局/terminal/final/finale`; every other nonblank role is ordinary metadata, with no combat semantics. If no entrance exists, use a nonterminal root. A single terminal-only room is allowed. If a requested graph requires ordinary templates but none exist, planning fails closed.

`roomCount` is 1..64, `maxAttempts` 1..100000; a single global budget covers root placements, tested target port/rotation combinations and tested corridor gaps, including rejected/backtracked attempts. Not a per-node reset. Budget does not enter RNG seed; it does enter instance identity. Exhaustion returns no partial Plan. `PlanningException` exposes `reason()` and `attempts()` and includes the last rejection. Coordinates use exact checked arithmetic; overflow rejects the entire call. No search-completeness or optimality guarantee under a finite budget.

`instanceId` is `ftd-` plus full SHA-256 of the complete seed material, room count, attempt budget, sorted matching-biome template metadata (including source hash and sorted ports), and sorted occupied boxes. Identical replay context has identical ID; changing pool metadata or occupied volumes changes ID. Callers must separately retain the manifest and occupied snapshot; Plan does not embed either.

## Geometry contract — important for runtime

1. Boxes are half-open. Face touching is allowed; positive-volume overlap is not. Room bounds transform all eight corners of `[0,size.x] × [0,size.y] × [0,size.z]` at the unchanged original origin. **Do not normalize origin to the rotated minimum.**
2. `rotate` applies the frozen point formulas exactly: q=0 `(x,z)`, q=1 `(-z,x)`, q=2 `(-x,-z)`, q=3 `(z,-x)`. `WorldPort.feet` is precisely that point transform, with Y unchanged. Local port feet identify a boundary opening block, not a guessed room center. North/west boundary index is 0, east is size.x-1, south is size.z-1.
3. Aperture width covers local transverse coordinates `[feet-floor((width-1)/2), +width)`, using X for north/south and Z for east/west. Even widths are supported by rotating the **whole aperture interval**, not merely its center. Width/height must match exactly across a connection; a narrow opening is not widened automatically. Feet y>=1 and feet.y+height<size.y preserve a floor and ceiling. Nonboundary/internal openings are rejected; catalog must provide real, evidence-backed exterior interfaces or separately revalidated adaptations.
4. `WorldPort.surface` is the true continuous outer room face, obtained by rotating aperture endpoints. `exteriorFeet` gives the canonical integer passage center immediately outside this face. Link waypoints are these exterior feet, not raw transformed metadata feet. The two endpoints are inclusive corridor block locations; no doorway block is implicitly owned by the corridor.
5. `corridorBounds(Link)` returns full reserved volumes: one floor layer, `height` air layers, one ceiling layer, plus one side-wall block on each side; no end cap over the opening. Planner reserves this complete shell against every room, prior corridor shell and occupied box. All room bases remain at anchor.y; local feet-height mismatches are refused rather than moving a room vertically to pretend stairs are supported.
6. **Voxel/MC placement offset:** a raw integer-position rotation is not the rotated minimum of a unit cube. To actually fill the frozen eight-corner bounds, `blockPosition(localBlock,origin,q)` applies rotation plus correction q0=(0,0,0), q1=(-1,0,0), q2=(-1,0,-1), q3=(0,0,-1). `blockPlacementOrigin(origin,q)` supplies the corresponding origin for a runtime whose block-index rotation matches the frozen formulas. E.g. q1 rotates geometric volume `[0,sx)×[0,sz)` to `[-sz,0)×[0,sx)`, not `[-sz+1,1)×[0,sx)`. Passing the uncorrected origin to MC integer block rotation leaks a one-block strip outside reserved bounds. Verify the actual MC rotation mapping and mirror/pivot settings in integration; do not blindly reuse `PlacedRoom.origin` as structure placement offset.

Core does not carve or seal doors. Runtime owns all template payloads and must preserve used openings and close unused openings without blocking the reserved paths. Portal foot records are points; voxel doorway cells must use `blockPosition`. Test fixtures explicitly enumerate aperture voxels under all 16 facing/rotation combinations and widths 1,2,3,4, proving these cells touch the corridor rather than falling outside the planned room volume.

## Run the real tests

From this directory in PowerShell:

```powershell
& .\build.ps1
```

The script records its absolute working directory and compiler/runtime paths in `logs/commands.log`. Current local toolchain is javac 25.0.2 with `--release 17 -encoding UTF-8 -Xlint:all -Werror`, then Microsoft OpenJDK **17.0.8** execution with `-ea`. This is actual Java 17 execution, not only a newer JVM running target-17 bytecode. Override `-Java`, `-Javac`, `-Jar` for another installation. Packaging uses jar's `--date` option (available since JDK19); using a JDK17-only jar binary requires separately packaging compiled production classes, or using JDK19+ jar. There are no downloaded dependencies.

Separate manual commands, if desired:

```powershell
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build\main (Get-ChildItem src\main\java -Recurse -Filter *.java).FullName
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp build\main -d build\test (Get-ChildItem src\test\java -Recurse -Filter *.java).FullName
& 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe' -ea -cp 'build\main;build\test' cn.ft.biomedungeon.core.CoreTests
```

Actual final result: **94,932 assertions passed**, including frozen-record reflection; seed golden vector; 0/90/180/270 rotations; non-normalized bounds; odd/even real doorway voxels; stable replay; shuffled template/port/occupied inputs; 200/200 distinct layouts and 200/200 branched trees; complete shell separation; blocked-root rejection and corridor reroute; malformed/empty/duplicate metadata; hard global retry cap; dry/wet, dimension and high-step incompatibility; no-partial-plan exhaustion; coordinate overflow; immutable outputs; 64-room maximum; 24 concurrent replays. Tests use synthetic metadata, not accepted catalog NBT.

Logs: `logs/tests-java17.log`, `logs/tests-java25.log`, `logs/cross-jvm-replay.log`, `logs/reproducible-build.log`, `logs/dependencies.log`, `logs/classfile-version.log`, `logs/jar-entries.log`. Java17/25 replay vectors and diversity metrics agree; two clean builds produce identical JAR hashes. `ready.json` carries exact source, artifact and evidence hashes. Consumer must recheck snapshot bytes; existence alone is not acceptance.

## Explicit limits

- Only straight, level corridors are generated. No stairs, slopes, elevators, single-bend routing, loops or terrain adaptation. `corridorBounds` can describe supplied level axis-aligned segments, but the planner does not generate bent links or claim support for their end/corner construction.
- Port medium matching is metadata-level only. Wet/wet planning does not certify fluid containment. World random ticks, water/lava/fire behavior, terrain, player ownership, block entities/entities, chunk tickets, placement budgets, persistent recovery and commit semantics are runtime/integration responsibilities.
- No MC world boundary/build-height policy is hard-coded in pure core; pass occupied volumes and perform runtime world-region prechecks on **both room bounds and corridor shells**, including the voxel placement convention above.
- No actual Forge world was started or modified by this component. No production instance was touched. No overall MVP/world-generation acceptance is claimed.
