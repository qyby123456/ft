scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 449 157 58 minecraft:air if block 449 158 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 449 157 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 466 157 72 minecraft:air if block 466 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 466 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 464 157 52 minecraft:air if block 464 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 464 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 157 72 minecraft:air if block 446 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 446 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 157 66 minecraft:air if block 456 158 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 456 157 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 460 157 59 minecraft:air if block 460 158 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 460 157 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 453 157 52 minecraft:air if block 453 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 453 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 464 157 64 minecraft:air if block 464 158 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 464 157 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 450 157 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 450 157 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b172 ftm_brick 2
