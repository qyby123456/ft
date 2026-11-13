scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1203 151 198 minecraft:air if block 1203 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1203 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1193 151 202 minecraft:air if block 1193 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1193 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1208 151 202 minecraft:air if block 1208 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1208 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1196 151 198 minecraft:air if block 1196 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1196 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1200 151 202 minecraft:air if block 1200 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1200 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1197 151 201 minecraft:air if block 1197 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1197 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1199 151 199 minecraft:air if block 1199 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1199 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1204 151 201 minecraft:air if block 1204 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1204 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1204 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1204 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b385 ftm_brick 2
