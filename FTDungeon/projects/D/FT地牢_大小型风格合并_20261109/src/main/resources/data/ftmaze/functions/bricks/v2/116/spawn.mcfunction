scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 76 151 60 minecraft:air if block 76 152 60 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 76 151 60 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 88 151 74 minecraft:air if block 88 152 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 88 151 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 72 minecraft:air if block 70 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 151 61 minecraft:air if block 90 152 61 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 90 151 61 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 151 68 minecraft:air if block 81 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 81 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 63 minecraft:air if block 70 152 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 151 74 minecraft:air if block 77 152 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 77 151 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 83 151 58 minecraft:air if block 83 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 83 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 77 151 60 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 77 151 60 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b116 ftm_brick 2
