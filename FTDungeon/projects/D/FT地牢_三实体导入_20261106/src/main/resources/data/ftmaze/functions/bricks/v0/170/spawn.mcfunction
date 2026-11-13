scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 502 151 98 minecraft:air if block 502 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 502 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 514 151 115 minecraft:air if block 514 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 514 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 151 112 minecraft:air if block 495 152 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 495 151 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 514 151 95 minecraft:air if block 514 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 514 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 506 151 108 minecraft:air if block 506 152 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 506 151 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 515 151 105 minecraft:air if block 515 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 515 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 151 101 minecraft:air if block 495 152 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 495 151 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 502 151 115 minecraft:air if block 502 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 502 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 502 151 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 502 151 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b170 ftm_brick 2
