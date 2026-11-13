scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 500 151 62 minecraft:air if block 500 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 500 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 517 151 54 minecraft:air if block 517 152 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 517 151 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 512 151 72 minecraft:air if block 512 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 512 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 504 151 52 minecraft:air if block 504 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 504 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 509 151 59 minecraft:air if block 509 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 509 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 497 151 70 minecraft:air if block 497 152 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 497 151 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 506 151 67 minecraft:air if block 506 152 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 506 151 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 515 151 64 minecraft:air if block 515 152 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 515 151 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 501 151 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 501 151 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b173 ftm_brick 2
