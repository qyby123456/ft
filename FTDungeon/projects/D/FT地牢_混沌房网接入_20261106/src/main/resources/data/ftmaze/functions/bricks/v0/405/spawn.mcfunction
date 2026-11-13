scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1521 31 198 minecraft:air if block 1521 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1521 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1511 31 202 minecraft:air if block 1511 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1511 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1526 31 202 minecraft:air if block 1526 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1526 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1514 31 198 minecraft:air if block 1514 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1514 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1518 31 202 minecraft:air if block 1518 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1518 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1515 31 201 minecraft:air if block 1515 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1515 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1517 31 199 minecraft:air if block 1517 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1517 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1522 31 201 minecraft:air if block 1522 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1522 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1522 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1522 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b405 ftm_brick 2
