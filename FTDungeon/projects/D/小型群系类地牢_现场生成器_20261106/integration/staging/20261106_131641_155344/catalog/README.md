# Catalog v1: unchanged independently accepted real-room MVP

## Consume this exact pool

Copy this directory's `data/` tree into the runtime JAR resource root. The entry point is `data/biomedungeon/manifest.json`; root `manifest.json` is a byte-identical review copy. Do not package `core-snapshot/`, Python tooling or the original assembled large NBTs as templates. `ready.json` and `SHA256SUMS.json` freeze the delivered source/resources/evidence/tests. This is a catalog component handoff, not overall runtime acceptance.

The minimal biome key is **s8** (purple crystal/geode). There are two distinct real source rooms, not two rotations of a combined dungeon:

| ID | Original room | Size | Real interfaces | Original SHA-256 |
|---|---|---|---|---|
| s8_05 | S8-05 straight passage | 15 x 12 x 43 | north (7,2,0), south (7,2,42); width 5, height 3, dry | fb8302274c4f0865c721669581e2714363f4ac49f5ebe22a12d3c7dfafd19837 |
| s8_06 | S8-06 L passage | 27 x 13 x 27 | north (6,2,0), east (26,2,20); width 5, height 3, dry | a22b7225717bc69a44e1e6400f792dd3e8b67ff11b52fd7114fb96d958fc9470 |

Both roles remain **normal**. There is no specially certified entrance/treasure/combat room in this MVP. Core can choose a normal root and reuse these two templates for a larger graph. This pool proves real room-level randomized straight/corner assembly, not completion of the original nine-room biome or its gameplay.

## Schema and evidence

`schemaVersion` is integer 1. `templates` contains `id`, `biome`, `resourceId`, `sha256`, `size`, `ports`, `role`, plus `accepted:true` and `independentAcceptance`. `size` and each port's `feet` are integer `{x,y,z}` objects. Every port has unique `id`, local boundary feet, compass `facing`, real odd `width`, real `height`, and observed `medium` (`dry`). Dimensions and aperture cells come from decoded original NBT plus its exact hash-bound `room.json`, never inferred room centers. `resourceId` is strictly `biomedungeon:<id>`, resolving only to `data/biomedungeon/structures/<id>.nbt`.

`independentAcceptance` records the independently indexed room source/hash, frozen file audit, hash-bound original metadata, original acceptance records and prior independent route output. Snapshot relative paths are catalog audit evidence, **not runtime paths**. Absolute provenance source paths are archival labels and must never be followed by the runtime resource loader. `unchangedBytes:true` means no state, palette, air, entity or port edits were made. The independent acceptance scope was sequential architectural production, not game deployment or combat.

The combined `layout.json` is read and frozen only for comparison. Some assembly interfaces were shifted/adapted compared with original rooms (including S8-07); original NBT interfaces cannot inherit them. `source-audit.json` audits 72 base-round single-room candidates; `source-inspection.json` is an earlier **layout diagnostic**, not an authoritative port pool. `rejections.json` lists all 78 excluded/deferred base-index entries (including all eight #10 overviews), and a blanket rejection of round-two #11-20 and combined assemblies. A conservative exclusion does not revoke prior architectural acceptance.

## Safety contract and limitations

- No source was adapted, no old acceptance hash was applied to new bytes. Both selected sources explicitly encode their full rectangular volume, including air, and contain no fluid/waterlogged states, fire, falling blocks, random-tick materials, entity payloads or block-entity blocks/payloads. A small explicit palette allowlist makes these two compatible with the runtime's current fail-closed restrictions. This does not establish general Minecraft block safety for other palettes.
- Original openings remain open. **Runtime must seal every unused port and the whole allocated envelope without blocking used links**, and preflight original air writes as destructive writes. Catalog bytes must not be silently capped or flattened. No per-room shell has been added by catalog.
- Boundary plus one-cell inward aperture/floor strips are checked directly; three-high feet-center BFS connects all advertised ports. Tests do not claim full player collision/step physics, arbitrary-width body movement, world placement, amethyst attachment updates, Minecraft-native block-state rotation, neighbor updates, random/fluid ticks, restart recovery or combat.
- All four coordinate rotations are invertible and preserve actual aperture cells; native world orientation acceptance is **NOT_RUN** here and must be completed in isolated runtime/integration tests before opening an instance to players. No mirror, wet interface, asymmetric/even-width interpretation or steep/stair port adaptation is certified.
- More biome pools are **deferred**, with actual decoded size/fluids/payloads/metadata reasons in `source-audit.json` and `ISSUES.json`. The primary restrictions are fluid handling, block entities/falling/dynamic palette states, and assembly-vs-source interface differences. Excluded sources are not copied as formal templates.

## Reproduce

Working directory is this `catalog` directory. Python uses the configured local executable `C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe`, **only as a Python interpreter**; no image service/API is called and no API environment/key is used.

1. `python -B freeze_catalog.py` reads live original sources read-only, checks all 80 base-index entries and copies the two exact source files; it fails if their source/evidence/metadata hashes or real apertures no longer match.
2. `python -B test_catalog.py` runs seven local tests: schema/packaging; source/copy/evidence SHA; actual full-volume NBT/ports; three-high internal route connectivity; all coordinate rotations; altered/obstructed aperture and tamper negatives; overview/future-pool exclusions.
3. `javac --release 17 -d test-classes core-snapshot/DungeonApi.java core-snapshot/DungeonGeometry.java core-snapshot/DungeonPlanner.java CatalogCoreSmoke.java` then `java -cp test-classes CatalogCoreSmoke` uses a read-only captured copy of the real core implementation, not a stub. Captured core hashes are included in the inventory. Compilation targeted Java 17; execution on available Java 25.0.2 is explicitly recorded, not misreported as Java 17 execution.
4. `python -B finalize_report.py` verifies the successful logs and writes immutable-file inventory plus component ready/progress metadata.

Actual result: 7/7 Python tests PASS; real-core smoke PASS for 32 seeds, six rooms/five links each, reversed input ordering reproducibility and 32 distinct layouts using both real template IDs. `tests.log`, `core-compile.log`, `core-smoke.log` record these runs. Minecraft world tests remain NOT_RUN.

The vendored `nbtio.py` is an unchanged copy of the existing original instance's `output/dungeon-expansion-20261101/tools/nbtio.py`; exact source and SHA are in `provenance.json`. All outputs were written only under this catalog directory. No worlds, installed mods/configuration or other component source files were modified.
