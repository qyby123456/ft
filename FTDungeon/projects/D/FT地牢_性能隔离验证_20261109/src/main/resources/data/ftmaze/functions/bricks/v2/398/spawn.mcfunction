scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1143 31 198 minecraft:air if block 1143 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1143 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1133 31 202 minecraft:air if block 1133 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1133 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1148 31 202 minecraft:air if block 1148 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1148 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1136 31 198 minecraft:air if block 1136 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1136 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1140 31 202 minecraft:air if block 1140 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1140 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1137 31 201 minecraft:air if block 1137 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1137 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1139 31 199 minecraft:air if block 1139 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1139 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1144 31 201 minecraft:air if block 1144 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1144 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1144 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1144 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b398 ftm_brick 2
