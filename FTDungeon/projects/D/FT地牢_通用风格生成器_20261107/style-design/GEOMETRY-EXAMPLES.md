# Geometry delta examples

The following counts measure explicit source AIR becoming a new non-AIR solid through the decoration recipe, after palette mapping. Palette-only block changes are excluded. These are reproducible static whitelist simulation results from verify-A.py; they are **not native Java collision results or a world-test pass**. Host/C must report actual StyleGeometry.process output counts separately.

| Template | Desert added solids | Frost added solids |
|---|---:|---:|
| straight/room_cells | 267 | 338 |
| straight/room_storage | 282 | 475 |
| straight/room_towers | 1272 | 1469 |
| turn/pillar_room | 401 | 385 |
| turn/big_doors | 895 | 991 |
| turn/small_doors | 108 | 73 |
| turn/hole | 1599 | 1558 |
| terminal/canteen | 1083 | 1277 |
| terminal/storage | 282 | 475 |
| terminal/cells | 1272 | 1469 |
| boss/eclipse_keeper | 362 | 1012 |
| promenade | 897 | 1632 |
| Total across eligible catalog variants, once each | 8720 | 11154 |

Forest has zero geometry changes. The total is a catalog comparison, not an instance total; a random plan can repeat or omit template variants. No new NBT template files are created.

Example custom selector using the existing field:

```json
{"block":"minecraft:quartz_bricks","mode":"air","role":"prefix:terminal/","from":[8,10,8],"to":[39,20,39],"spacing":5}
```

For all eligible non-Boss room types use role=rooms. For only the composite straight-room variants use role=room. For Boss only use role=boss; its coordinate box must fit the Boss dimensions. For all eligible types use role=all with a box fitting every matched template. Default recipes keep per-role bounds because the Boss is smaller than main rooms.

Native comparison method: retain palette-mapped source, call StyleGeometry.process, compare cells by BlockPos, count only old.state().isAir() and !new.state().isAir(); assert each original non-AIR/BE cell retains its state and NBT. Then run Planner against final cells to check complete portal clearance, supported runes and all walk routes. This worker does not modify host-owned native tests.
