scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 73 151 36 minecraft:air if block 73 152 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 73 151 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 151 51 minecraft:air if block 90 152 51 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 90 151 51 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 89 151 31 minecraft:air if block 89 152 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 89 151 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 51 minecraft:air if block 70 152 51 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 51 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 151 45 minecraft:air if block 80 152 45 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 80 151 45 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 83 151 37 minecraft:air if block 83 152 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 83 151 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 151 31 minecraft:air if block 78 152 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 78 151 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 151 40 minecraft:air if block 90 152 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 90 151 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 74 151 36 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 74 151 36 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b112 ftm_brick 2
