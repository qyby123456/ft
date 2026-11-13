scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1479 31 198 minecraft:air if block 1479 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1479 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1469 31 202 minecraft:air if block 1469 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1469 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1484 31 202 minecraft:air if block 1484 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1484 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1472 31 198 minecraft:air if block 1472 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1472 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1476 31 202 minecraft:air if block 1476 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1476 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1473 31 201 minecraft:air if block 1473 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1473 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1475 31 199 minecraft:air if block 1475 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1475 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1480 31 201 minecraft:air if block 1480 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1480 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1480 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1480 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b406 ftm_brick 2
