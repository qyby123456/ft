scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1101 31 198 minecraft:air if block 1101 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1101 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1091 31 202 minecraft:air if block 1091 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1091 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1106 31 202 minecraft:air if block 1106 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1106 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1094 31 198 minecraft:air if block 1094 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1094 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1098 31 202 minecraft:air if block 1098 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1098 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1095 31 201 minecraft:air if block 1095 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1095 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1097 31 199 minecraft:air if block 1097 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1097 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1102 31 201 minecraft:air if block 1102 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1102 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1102 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1102 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b397 ftm_brick 2
