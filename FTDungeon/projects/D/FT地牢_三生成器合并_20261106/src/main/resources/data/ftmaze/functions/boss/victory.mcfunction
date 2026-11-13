scoreboard players set #run ftm_state 4
fill 139 31 284 141 33 284 minecraft:air
fill 139 30 285 141 30 294 minecraft:sea_lantern
bossbar set ftmaze:final visible false
tellraw @a[tag=ftmaze.inside] {"text":"典狱长已击败，王座后的折返门开启。","color":"green"}
