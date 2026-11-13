scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 151 49 minecraft:air if block 193 152 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 193 151 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 151 39 minecraft:air if block 209 152 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 209 151 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 151 59 minecraft:air if block 206 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 206 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 196 151 39 minecraft:air if block 196 152 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 196 151 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 151 46 minecraft:air if block 203 152 46 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 203 151 46 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 58 minecraft:air if block 190 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 51 minecraft:air if block 210 152 51 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 51 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 198 151 55 minecraft:air if block 198 152 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 198 151 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 151 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 151 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b119 ftm_brick 2
