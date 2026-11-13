scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1119 151 198 minecraft:air if block 1119 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1119 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1109 151 202 minecraft:air if block 1109 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1109 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1124 151 202 minecraft:air if block 1124 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1124 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1112 151 198 minecraft:air if block 1112 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1112 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1116 151 202 minecraft:air if block 1116 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1116 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1113 151 201 minecraft:air if block 1113 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1113 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1115 151 199 minecraft:air if block 1115 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1115 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1120 151 201 minecraft:air if block 1120 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1120 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1120 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1120 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b383 ftm_brick 2
