scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 151 196 minecraft:air if block 22 152 196 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 22 151 196 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 151 206 minecraft:air if block 35 152 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 35 151 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 151 193 minecraft:air if block 37 152 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 37 151 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 151 204 minecraft:air if block 19 152 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 19 151 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 151 199 minecraft:air if block 30 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 30 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 151 192 minecraft:air if block 27 152 192 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 27 151 192 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 24 151 201 minecraft:air if block 24 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 24 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 151 200 minecraft:air if block 36 152 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 36 151 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 151 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 151 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b144 ftm_brick 2
