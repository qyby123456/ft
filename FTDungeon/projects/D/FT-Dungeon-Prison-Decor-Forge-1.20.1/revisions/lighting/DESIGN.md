# Lighting UV repair freeze / 21 双胆炼金灯、23 禁文立碑灯

## Status and correction of the first pass

This revision supersedes the first lane acceptance. That first acceptance was invalid for final-export visuals: its contact-sheet renderer stretched an entire 32px tile onto every cuboid face, while the parent exporter has always used Minecraft `default_uv`, cropped by model coordinates at two texels per model unit. The first image could show complete container frames, a large pressure dial and complete inscriptions that the real exported faces did not sample. The parent's actual `preview/before-after-12.png` was inspected: it showed the missing gauge, an overly broad cyan band and clipped rune fragments. The original claim of visual agreement is withdrawn. Reproducibility of the wrong first image did not prove UV compatibility.

The correction preserves all geometry and identity, redraws the original atlas for the true crop windows, and replaces the lane's preview path. It uses the parent's actual pure `default_uv`, `cells`, `face_visible` and `export_model` functions, read from `forge-project/tools/export_kit.py` through Python AST. The preview then uses the actual pure `face_vertices`, `uv_vertices` and `render` functions from `render_kit.py`, including their face orientation. No parent main routine, shared write, Blockbench state or other model is executed/changed. Candidate model dictionaries and diagnostic JSON stay under this lane's `work/`.

The corrected seven-file lane is a frozen **candidate for the parent's re-export**. It does not replace the final exported-model before/after image, LIT check, Forge build or final acceptance. Old shared texture files remain stale until the parent re-exports.

## Preserved model identities and real geometry costs

Target remains Minecraft Java 1.20.1 / Forge 47.4.10 / Java 17. Both are single-block, floor-mounted `decor`, with zero rotation and integer endpoints in 0..16.

| Model | Preserved identity | Source cuboids / faces | Fully internal faces | Candidate exported faces |
| --- | --- | ---: | ---: | ---: |
| 21 双胆炼金灯 | `prison_alchemical_lamp`, Twin Vessel Alchemical Lamp, light12, `minecraft:brewing_stand` | 23 / 138 | 30 | 108 |
| 23 禁文立碑灯 | `prison_rune_beacon`, Bound Rune Tablet, light10, `minecraft:glow_ink_sac` | 20 / 120 | 27 | 93 |

The face counts above come from the parent's actual in-memory exporter with full-face internal pruning, not an assumed six-faces-per-box runtime count. There are 43 cuboids, 258 source faces and 201 selected candidate faces across both independent models. Geometry and selected face UVs are compared read-only with the parent's existing exported JSON. Counts are not draw calls or FPS measurements.

21 remains an asymmetric compact distillation instrument: larger necked cyan retort left/back, smaller amber receiver right/forward, brass shoes, coherent front pressure-control housing, recessed slate deck and wide iron sole. A four-part face-connected metal bridge includes an elevated span, a depth-changing forward elbow and a condenser downcomer. The aperture `[9,8,6]`..`[12,14,8]` is physically empty, 36 model-unit cubes. Bounds `[0,0,1]`..`[16,15,13]`. Small gauge/valve details remain pixels; the housing was not increased in height to fit more artwork.

23 remains a thick carved stela with narrower stone shoulder/peak, load-bearing plinth, stone buttress feet, grounded iron posts, a closed four-sided iron sealing hoop, front/back binding keys and rising horns with inward hooks. Actual side openings remain `[3,9,7]`..`[5,11,9]` and `[11,9,7]`..`[13,11,9]`, eight model-unit cubes each. Bounds `[1,0,2]`..`[15,15,14]`. Dark iron dominates the restraint; brass is restricted to binding keys. It is a decorative level-10 light, not a vanilla beacon or gameplay effect.

## The actual UV contract

Every original 32x32 tile is exported as its own 32px texture. Java UV values are 0..16 within that texture; multiply by two for actual tile pixels. A face receives its coordinate-projected crop at a fixed 2px/model-unit density. No `face_uv` override or full-tile stretch is introduced. Atlas-region UV metadata describes extraction only and is explicitly named accordingly.

For bounds `(x0,y0,z0,x1,y1,z1)`, the exact parent default projections are:

| Face | Java UV |
| --- | --- |
| north | `[16-x1,16-y1,16-x0,16-y0]` |
| south | `[x0,16-y1,x1,16-y0]` |
| west | `[z0,16-y1,z1,16-y0]` |
| east | `[16-z1,16-y1,16-z0,16-y0]` |
| up | `[x0,z0,x1,z1]` |
| down | `[x0,16-z1,x1,16-z0]` |

The local copy is checked against the actual parent's function on all 258 declared faces. All 201 surviving exported face rectangles are recorded in `work/face-uv-inventory.json`, with exact crop and 2x2 density checks. The six key windows are also extracted directly from the final atlas, enlarged with nearest sampling only for diagnostic viewing, in `work/actual-uv-crops.png`.

| Critical face / tile | Actual tile-pixel rectangle, upper endpoint exclusive |
| --- | --- |
| 21 control housing north / 6 | `[18,24,28,28]`, 10x4 texels |
| 21 cyan central body north / 8 | `[16,12,26,24]`, 10x12 texels |
| 21 cyan central body south / 8 | `[6,12,16,24]`, 10x12 texels |
| 21 amber central body north / 11 | `[4,18,10,24]`, 6x6 texels |
| 21 amber central body south / 11 | `[22,18,28,24]`, 6x6 texels |
| 23 carved tablet north / 14 | `[10,8,22,24]`, 12x16 texels |

## Repainted pixel materials

One original opaque RGBA 128x128 atlas contains sixteen 32x32 tiles: 16,384 pixels, one frame, 65,536 raw RGBA bytes before mipmaps or GPU/atlas overhead. Alpha is 255 everywhere. No transparency, cutout, bloom, shells or animation copies are used. Pixels are deliberate clusters, never random noise.

Ordinary tiles 0..7 are iron, inset slate top, dark underside, aged brass, continuous horizontal pipe, continuous vertical pipe, gauge face and cold carved stone. Narrow rods now sample continuous material fields and controlled iron light bands, rather than squeezing a complete framed/riveted tile onto every rod. Actual collars/shoes carry brass. The gauge is a small 4x4-texel rounded dial inside the 10x4 face crop, with three ivory center pixels, one dark needle pixel and restrained marks to its right. It is intentionally simple enough for this real pixel budget.

Glow-only cyan tiles 8..10 and amber tiles 11..13 carry distinct body fronts/backs, side fields and transition glass. Main cyan liquid is aligned to global y7, texture row18; amber liquid is aligned to global y5.5, row21. On every relevant projected vertical body face, the meniscus is exactly one texture row (0.5 model unit) thick, with darker empty upper container, separate lower liquid and a broken vertical reflection. Shared side facets use the same world-height row. The original broad misplaced stripe is removed. Neck/shoulder/foot material uses simpler continuous glass fields; no repeated full bottle border is stretched onto tiny faces.

Glow-only tiles 14..15 are the rune-bearing front stone and quiet side/reverse carved stone. Two cyan angular inscription groups lie completely within the actual 12x16 north crop, above and below the hoop's y8..9 obstruction band. The final crop contains 22 bright cyan pixels / 192 = 11.46%; whole tile coverage is 22/1024 = 2.15%. The bright strokes are explicitly checked for north-view occlusion against the actual front iron volumes: none are hidden. The side/reverse has chisel relief and tiny muted cyan accents. Keeping the rune-bearing stone dedicated lets the parent dim it without recoloring the ordinary base/crown; whole rune-bearing stone may darken too. This does not promise shader emissivity.

Glowing and ordinary tiles are strictly disjoint across both models: 0..7 ordinary, 8..15 glowing. Parent still owns identical-geometry LIT states and actual block-light behavior.

## Current checks and visual review

Automated checks cover: exactly two preserved IDs/kind/mount/light/ingredient fields; <=28 boxes each; positive integer 0..16 volumes; zero rotations; no positive-volume intersections, occupied-voxel duplication, same-normal coplanar surface duplication or zero-contribution parts; all parts face-connected to the floor; continuous cap-to-cap pipe and closed iron hoop; required empty apertures; tile index range; no ordinary/glow sharing; atlas size and full opacity; exact parent UV agreement, density and crop windows; gauge presence, world-height meniscus and unobscured rune strokes; four views and native 64px icons; reproducibility.

The contact sheet now uses actual parent export/render helpers at yaw42/138/222/318 and pitch27, normal parent flat lighting and nearest sampling, with candidate atlas pixels. It includes four views and four 64x64 icons for each model. Icons render at the unchanged parent's 64/27 scale into a 64x80 padding buffer and crop y12..76, removing only the parent's +4px vertical offset so the floor cannot clip; there is no image resampling or UV change. Clay reviews use the same exported geometry and parent renderer. The old custom full-tile rasterizer and its reversed UV face ordering are removed.

Visual inspection covers the corrected contact sheet, atlas, six actual UV crop samples, both clay sheets and both native front icons. The gauge is present, both vessel fronts/backs show container and liquid hierarchy, and the tablet shows two complete separated glyph groups in the real sampled area. The new image and atlas hashes are frozen in the source and checked on generation; a mismatch requires fresh inspection. Earlier full-tile preview hashes and the inspected failed final before/after hash remain in `qa.json` as the explicitly withdrawn first-pass evidence.

## Reproduction, limits and handoff

Run with configured Python, Pillow and NumPy, keeping the parent `forge-project/tools/export_kit.py` and `render_kit.py` available beside the revision hierarchy:

```powershell
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' ./make_assets.py
```

The script reads the parent's pure helpers and current exported JSON but writes only beside itself or under its own `work/`. No shared module import/main or shared pycache is generated. It creates two complete builds in separate lane directories and compares definitions, atlas, tilemap, contact sheet and QA byte-for-byte, then adds identical reproducibility metadata and compares QA again. Source, design, parent exporter, parent renderer and artifact hashes are recorded. There is no randomness or timestamp. Fonts affect review labels on a different host, not atlas pixels or geometry.

Seven top-level files remain: `definitions.json`, `atlas.png`, `tilemap.json`, `make_assets.py`, `DESIGN.md`, `contact-sheet.png`, `qa.json`. Diagnostics, candidate exported JSON, UV inventory/crops, clay views and repeat builds stay under `work/`.

No shared model, mod, save, runtime source or Blockbench was changed; no game, client or server was launched. This lane does not claim final shared-resource export, final LIT pixels, Forge build, packaged resource acceptance, game visuals, light propagation, FPS, draw calls or GPU memory. The parent must re-export, rebuild and inspect its new actual before/after and both LIT states before final acceptance. The lane contact sheet is supporting evidence, not a substitute for those final checks.
