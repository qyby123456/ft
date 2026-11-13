scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1105 31 198 minecraft:air if block 1105 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1105 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1095 31 202 minecraft:air if block 1095 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1095 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1110 31 202 minecraft:air if block 1110 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1110 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1098 31 198 minecraft:air if block 1098 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1098 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1102 31 202 minecraft:air if block 1102 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1102 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1099 31 201 minecraft:air if block 1099 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1099 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1101 31 199 minecraft:air if block 1101 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1101 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1106 31 201 minecraft:air if block 1106 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1106 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1106 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1106 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b359 ftm_brick 2
