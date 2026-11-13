scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 151 195 minecraft:air if block 22 152 195 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 22 151 195 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 151 206 minecraft:air if block 38 152 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 38 151 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 151 206 minecraft:air if block 18 152 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 18 151 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 151 193 minecraft:air if block 35 152 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 35 151 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 151 199 minecraft:air if block 30 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 30 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 151 202 minecraft:air if block 23 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 23 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 151 192 minecraft:air if block 27 152 192 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 27 151 192 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 151 199 minecraft:air if block 19 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 19 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 151 196 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 151 196 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b144 ftm_brick 2
