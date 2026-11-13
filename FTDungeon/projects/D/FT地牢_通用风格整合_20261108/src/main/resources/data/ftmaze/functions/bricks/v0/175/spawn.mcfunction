scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 449 157 25 minecraft:air if block 449 158 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 449 157 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 466 157 17 minecraft:air if block 466 158 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 466 157 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 461 157 33 minecraft:air if block 461 158 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 461 157 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 157 16 minecraft:air if block 454 158 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 454 157 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 459 157 23 minecraft:air if block 459 158 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 459 157 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 157 33 minecraft:air if block 446 158 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 446 157 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 157 18 minecraft:air if block 446 158 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 446 157 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 157 30 minecraft:air if block 454 158 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 454 157 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 450 157 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 450 157 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b175 ftm_brick 2
