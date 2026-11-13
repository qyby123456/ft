# Lighting / Ironwork — original prison kit, 2026-11-03

## Deliverable and boundary

Eight original, single-block Minecraft Java 1.20.1 / Forge authoring definitions for namespace `ftdungeonprops`. The required files are `definitions.json`, `atlas.png`, `tilemap.json`, `make_assets.py`, `DESIGN.md`, and `contact-sheet.png`. All generated sources, previews and QA files stay inside this lighting lane. No existing FT project, save, installed mod or shared Blockbench session was opened for editing. The parent owns runtime code, final Java models, `.bbmodel` files, recipes and client QA.

Rebuild from the script directory using Python 3.12 + Pillow 13 (the supplied bundled interpreter). `make_assets.py` deterministically overwrites only its own generated files below its directory. It uses no network, random noise, image API, paid service or copied texture. Contact-sheet typography uses Windows Microsoft YaHei and Consolas; a Pillow fallback exists but will change preview typography on other systems. Geometry and atlas pixels do not depend on fonts.

```powershell
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' './make_assets.py'
```

## Shape and material decisions

1. **囚室巡夜灯 / Cell Watch Lantern:** four raised feet, six offset cage guards, a narrow amber burner, brass stepped cap and open carry bail. The sightlines at x=5 and x=10, y=7 remain empty through the cage. This is not the first batch's ten-box cyan soul lantern: the elongated tray, six-bar layout, four feet and small amber chimney are newly authored.
2. **狱吏悬臂灯 / Warden Bracket Lantern:** a service plate at the back, horizontal cantilever and short down-link carry an actual open lantern. This replaces the old wall-cresset/bowl language with a suspended ironwork silhouette; no wall niche or opaque flame-filled bowl is reused.
3. **四臂锁环吊灯 / Four Arm Iron Chandelier:** a hollow square ring carries four brass candle pans. Four tension links reach split cruciform beams, a main suspension stem and the fixing plate at y=16. The split beams avoid intersecting central volumes. The central ring opening is real, not painted black.
4. **守夜融蜡烛丛 / Vigil Candle Cluster:** an iron drip tray with four rim rails, a settled wax pool, three deliberately uneven candles and one burnt stub. Lit flame tops are y=14, 10 and 6. Only the three small flames glow; wax bodies and their long melt-flow clusters remain ordinary opaque material. Shoulders are structural two-step wax masses, not per-pixel extrusion.
5. **双胆炼金灯 / Twin Vessel Alchemical Lamp:** asymmetric large cyan retort and small amber receiver, brass split clamp, copper transfer elbow, valve and front pressure gauge on a teal enamel instrument foot. The front gauge is a dedicated face override. All vessels are opaque stylized fluid/crystal bodies, not glass or alpha planes. It has no brewing or inventory behavior.
6. **锁座寒晶簇 / Socketed Frost Crystal:** stone wall backplate and projecting corbel support three separate brass sockets. Three solid crystal bodies terminate at different heights with pale stepped crystal tips. No crystal floats or intersects its mounting.
7. **禁文立碑灯 / Bound Rune Tablet:** a double-faced engraved tablet is held between two carved stone piers, a lintel and four bronze retaining claws on a layered plinth. The rune was drawn specifically for this atlas. It is a decorative lamp, NOT a vanilla beacon: no beam, status effects, inventory or beacon mechanics.
8. **铁栅囚窗 / Iron Barred Prison Window:** four thick stone frame members, three vertical forged bars, a forward locking rail, a keystone and projecting sill. The 10×10-unit aperture retains exactly 63/100 completely open front-to-back unit columns. Alpha is 255 everywhere; there is no glass, fake transparent hole, teleportation or emission. Installation into an opaque full wall does not magically cut the adjacent wall; the opening must be built into the architecture. Runtime collision follows the actual union, not a promised walk-through portal.

## Geometry and mount contract

NORTH/front is z=0, wall/back is z=16. Floor models touch y=0, wall fixtures touch z=16, and the chandelier touches y=16. Every endpoint is an integer within 0..16. Every box has positive volume and zero rotation. Every model is one face-connected structural assembly, including flames, sockets, beams and mounting hardware. All pairwise positive-volume overlap and duplicate-box checks pass; no hidden zero-contribution box is retained. Touching surface regions are deliberate structural joints, not duplicate visible faces. The parent may prune/split internal contacting face regions during final export.

### Costs — source definitions, not GPU measurements

Budget was set before generation: maximum 28 boxes each, 168 unpruned declared rectangle faces each, one shared static 128×128 opaque atlas, zero transparent planes, zero outline shells, zero animations or frame copies. Planned source counts were 19/14/25/16/17/11/11/10 and actual counts match.

| ID | Name | Boxes | Unpruned face ceiling | Glow boxes | Mount | Block light |
|---|---|---:|---:|---:|---|---:|
| `prison_cell_lantern` | 囚室巡夜灯 | 19 | 114 | 2 | floor | 12 |
| `prison_wall_lantern` | 狱吏悬臂灯 | 14 | 84 | 2 | wall | 12 |
| `prison_iron_chandelier` | 四臂锁环吊灯 | 25 | 150 | 4 | ceiling | 14 |
| `prison_candle_cluster` | 守夜融蜡烛丛 | 16 | 96 | 3 | floor | 11 |
| `prison_alchemical_lamp` | 双胆炼金灯 | 17 | 102 | 3 | floor | 12 |
| `prison_wall_crystal` | 锁座寒晶簇 | 11 | 66 | 6 | wall | 13 |
| `prison_rune_beacon` | 禁文立碑灯 | 11 | 66 | 1 | floor | 10 |
| `prison_barred_window` | 铁栅囚窗 | 10 | 60 | 0 | wall | 0 |

Total source cost: **123 structural boxes / 738 unpruned face ceiling**. All eight are static. Counts are per selected model, not sixteen LIT-state copies loaded together. Seven lamps are intended for parent-provided LIT and horizontal FACING; the window has horizontal FACING but no light. Materials are sixteen semantic atlas tiles, not sixteen measured draw calls. One RGBA atlas is 16,384 texels / 65,536 base bytes, excluding mips, GPU alignment and engine copies; this is not measured VRAM.

| ID | Fully hidden rectangle faces | Partial-contact rectangle faces | Rectangles with some exterior area | Exterior unit area |
|---|---:|---:|---:|---:|
| `prison_cell_lantern` | 24 | 9 | 90 | 772 |
| `prison_wall_lantern` | 18 | 8 | 66 | 736 |
| `prison_iron_chandelier` | 28 | 21 | 122 | 728 |
| `prison_candle_cluster` | 17 | 12 | 79 | 628 |
| `prison_alchemical_lamp` | 21 | 20 | 81 | 690 |
| `prison_wall_crystal` | 11 | 15 | 55 | 684 |
| `prison_rune_beacon` | 15 | 14 | 51 | 720 |
| `prison_barred_window` | 14 | 10 | 46 | 898 |

These are exact unit-grid surface statistics for the authoring boxes. A partially contacted face can require splitting when exported, so neither the exterior-rectangle column nor the QA adapter's face count is a claim about final parent-export quads. The adapter removes only faces proven fully internal; it never deletes a partially visible face wholesale.

## Atlas and UV

The original RGBA atlas is exactly 128×128, four columns by four rows of 32×32. Tile index is row-major; pixel bounds and Java 0..16 UV rectangles are explicit in `tilemap.json`. Intended rendering is nearest filtering with one complete tile per face; dedicated gauge/rune motifs must not be cropped to a tiny corner. Small bars therefore intentionally use a stretched reusable surface, not a globally uniform texel-density unwrap. This authoring schema has no per-box UV-size field. Palette clusters, forged streaks, copper oxidation, stone chips, wick marks and wax drips are rasterized directly with no smoothing, stochastic TV noise or pixel-to-box extrusion.

Tiles **0–11** are structure-only: iron, riveted plate, cold stone, socket stone, brass, copper, wax, char, enamel, gauge, service plate and wax pool. Tiles **12–15** are exclusive to `glow:true` boxes: amber flame, frost core, rune tablet, cyan vessel/crystal. Every default tile and face override is checked against this partition. No glowing tile is reused by non-glowing geometry, including candle wax and the entire window. The rune's side-face overrides remain within the glowing partition.

Parent integration should darken only the dedicated glowing tiles for LIT=false; it must not dim unrelated wax, brass, stone or gauge faces. Java 1.20.1 material albedo alone does not provide shader emissivity or bloom. The CPU preview adds neither. OFF thumbnails illustrate a 0.22 multiplier on glowing tiles, not the parent's finalized dark palette. The DIM ON diagnostic scales all surfaces by 0.35 and is a readability comparison, not a physical lighting simulation. Main-hand toggle handling, actual block light state and collision are parent work, not implemented here.

## Reference and originality

Read-only reference: [Mine Cells 2.0.0+1.20.1](https://github.com/mim1q/MineCells/tree/minecells-2.0.0%2B1.20.1), including its public prison guide image and asset-tree names. Borrowed ideas are limited to cool damp prison masonry, weathered iron, warm/cyan contrast, cage negative space and compact alchemical equipment. The upstream README states that textures/models are **All Rights Reserved**; none is copied, bundled, traced, recolored or redistributed here. All eight coordinates, all sixteen tile drawings and the rune are newly authored from primitives. Existing first-batch lantern/cresset source was inspected read-only solely to avoid repeating its silhouette. No upstream or first-batch image is included in this lane.

## Actual checks and preview evidence

`make_assets.py` runs fail-fast checks for the exact eight IDs, kind/mount/light schema, 28-box budget, all coordinate bounds and integer types, positive volume, zero rotation, unique part names, duplicate boxes, occupied-volume intersections, connected structural assembly, hidden boxes, mount contact, all four horizontal-facing bounds, all tile references and face overrides, strict glow tile segregation, atlas size/mode/alpha, and reopened-file round trips. Window openness, lantern through-sightlines and chandelier ring openness have explicit feature assertions. The generated `work/qa/validation-report.json` records exact counts, complete limitations and detailed per-box unit-face coverage.

- `contact-sheet.png`: eight neutral-light front-right authoring renders, rear miniatures, and illustrative OFF miniatures, plus all sixteen original material swatches.
- `work/qa/six-views.png`: all eight models in front, back, east, west, up and down orthographic projections with identical 0..16 framing.
- `work/qa/clay-and-lighting.png`: all eight in untextured clay, neutral ON, dim ON and neutral OFF conditions, with fixed projection and no bloom.
- `work/qa/java-adapter/assets/`: isolated QA-only Java JSON conversion with explicit UV, one self-contained atlas and fully internal full faces removed. It is not the final parent export and is not installed anywhere.
- `work/qa/artifact-hashes.json`: SHA-256 of required deliverables for deterministic rebuild comparison.

Generator checks and offline projections are performed here. The delivered isolated QA adapter was additionally checked read-only with the skill's `audit_model.py`: **8/8 pass, zero errors, zero warnings, all texture dependencies resolved, 123 elements and 590 declared adapter faces**. Eight detailed `work/qa/*.audit.json` reports preserve the audit's limitations. The audit uses the closest available `java-legacy` structural profile; that profile is not a complete Java 1.20.1 certification. The parent independently checks final compiled models, `.bbmodel` import, actual LIT/FACING behavior, recipes, loot and in-game presentation. **No shared editor inspection, Minecraft client/server launch, Display-slot validation, runtime collision test, performance measurement or FPS claim is made.** The CPU renderer is original, orthographic and uses full-tile UV; it is not Blockbench or Minecraft. A final game render can differ in ambient occlusion, lighting, UV policy and item transforms.

API/service cost: **0 paid image requests / 0 image API charges**. Source and atlas are authored locally. Public GitHub reads were reference inspection only. No token-price or external runtime-cost estimate is invented.
