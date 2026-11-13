scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1479 151 198 minecraft:air if block 1479 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1479 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1469 151 202 minecraft:air if block 1469 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1469 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1484 151 202 minecraft:air if block 1484 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1484 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1472 151 198 minecraft:air if block 1472 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1472 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1476 151 202 minecraft:air if block 1476 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1476 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1473 151 201 minecraft:air if block 1473 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1473 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1475 151 199 minecraft:air if block 1475 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1475 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1480 151 201 minecraft:air if block 1480 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1480 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1480 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1480 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b404 ftm_brick 2
