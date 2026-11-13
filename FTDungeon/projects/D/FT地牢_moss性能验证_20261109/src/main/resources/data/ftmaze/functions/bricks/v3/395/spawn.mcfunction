scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1101 151 198 minecraft:air if block 1101 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1101 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1091 151 202 minecraft:air if block 1091 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1091 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1106 151 202 minecraft:air if block 1106 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1106 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1094 151 198 minecraft:air if block 1094 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1094 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1098 151 202 minecraft:air if block 1098 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1098 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1095 151 201 minecraft:air if block 1095 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1095 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1097 151 199 minecraft:air if block 1097 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1097 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1102 151 201 minecraft:air if block 1102 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1102 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1102 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1102 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b395 ftm_brick 2
