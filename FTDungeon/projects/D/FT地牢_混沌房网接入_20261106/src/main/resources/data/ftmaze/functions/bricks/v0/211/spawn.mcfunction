scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1166 151 71 minecraft:air if block 1166 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1166 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1183 151 86 minecraft:air if block 1183 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1183 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1182 151 66 minecraft:air if block 1182 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1182 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 151 86 minecraft:air if block 1163 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1163 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 151 80 minecraft:air if block 1173 152 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1173 151 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 151 72 minecraft:air if block 1176 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1176 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1171 151 66 minecraft:air if block 1171 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1171 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 151 77 minecraft:air if block 1181 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1181 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1167 151 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1167 151 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b211 ftm_brick 2
