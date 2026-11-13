scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1143 151 198 minecraft:air if block 1143 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1143 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1133 151 202 minecraft:air if block 1133 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1133 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1148 151 202 minecraft:air if block 1148 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1148 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1136 151 198 minecraft:air if block 1136 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1136 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1140 151 202 minecraft:air if block 1140 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1140 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1137 151 201 minecraft:air if block 1137 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1137 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1139 151 199 minecraft:air if block 1139 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1139 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1144 151 201 minecraft:air if block 1144 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1144 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1144 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1144 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b396 ftm_brick 2
