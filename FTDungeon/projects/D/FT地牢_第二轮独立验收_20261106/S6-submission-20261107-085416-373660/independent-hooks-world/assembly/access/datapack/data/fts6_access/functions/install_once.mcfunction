forceload add ~ ~ ~206 ~179
place template fts6_access:s6_0_0_0 ~0 ~0 ~0
place template fts6_access:s6_0_0_48 ~0 ~0 ~48
place template fts6_access:s6_0_0_96 ~0 ~0 ~96
place template fts6_access:s6_0_0_144 ~0 ~0 ~144
place template fts6_access:s6_48_0_0 ~48 ~0 ~0
place template fts6_access:s6_48_0_48 ~48 ~0 ~48
place template fts6_access:s6_48_0_96 ~48 ~0 ~96
place template fts6_access:s6_48_0_144 ~48 ~0 ~144
place template fts6_access:s6_96_0_0 ~96 ~0 ~0
place template fts6_access:s6_96_0_48 ~96 ~0 ~48
place template fts6_access:s6_96_0_96 ~96 ~0 ~96
place template fts6_access:s6_96_0_144 ~96 ~0 ~144
place template fts6_access:s6_144_0_0 ~144 ~0 ~0
place template fts6_access:s6_144_0_48 ~144 ~0 ~48
place template fts6_access:s6_144_0_96 ~144 ~0 ~96
place template fts6_access:s6_144_0_144 ~144 ~0 ~144
place template fts6_access:s6_192_0_0 ~192 ~0 ~0
place template fts6_access:s6_192_0_48 ~192 ~0 ~48
place template fts6_access:s6_192_0_96 ~192 ~0 ~96
place template fts6_access:s6_192_0_144 ~192 ~0 ~144
fill ~-10 ~0 ~32 ~-1 ~3 ~40 minecraft:polished_blackstone_bricks
fill ~-10 ~4 ~32 ~-1 ~7 ~40 minecraft:air
fill ~-10 ~4 ~32 ~-10 ~5 ~40 minecraft:orange_stained_glass
fill ~-1 ~4 ~32 ~-1 ~5 ~40 minecraft:orange_stained_glass
fill ~-9 ~4 ~32 ~-2 ~5 ~32 minecraft:orange_stained_glass
fill ~-9 ~4 ~40 ~-2 ~5 ~40 minecraft:orange_stained_glass
fill ~-7 ~4 ~40 ~-4 ~5 ~40 minecraft:air
setblock ~-6 ~3 ~36 minecraft:chiseled_polished_blackstone
summon minecraft:marker ~ ~ ~ {Tags:["fts6_origin"]}
summon minecraft:marker ~-5.5 ~4 ~36.5 {Tags:["fts6_lobby"]}
forceload remove ~ ~ ~206 ~179
forceload add ~ ~
forceload add ~-6 ~36
