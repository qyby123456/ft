scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1554 151 55 minecraft:air if block 1554 152 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1554 151 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1567 151 70 minecraft:air if block 1567 152 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1567 151 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1547 151 69 minecraft:air if block 1547 152 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1547 151 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1558 151 65 minecraft:air if block 1558 152 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1558 151 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1566 151 57 minecraft:air if block 1566 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1566 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1547 151 58 minecraft:air if block 1547 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1547 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1554 151 72 minecraft:air if block 1554 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1554 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1561 151 52 minecraft:air if block 1561 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1561 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1554 151 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1554 151 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b231 ftm_brick 2
