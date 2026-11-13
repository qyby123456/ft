scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1550 151 24 minecraft:air if block 1550 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1550 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1567 151 38 minecraft:air if block 1567 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1567 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 151 18 minecraft:air if block 1565 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1565 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1547 151 38 minecraft:air if block 1547 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1547 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 151 32 minecraft:air if block 1557 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1557 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1561 151 25 minecraft:air if block 1561 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1561 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1554 151 18 minecraft:air if block 1554 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1554 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 151 30 minecraft:air if block 1565 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1565 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1551 151 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1551 151 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b234 ftm_brick 2
