scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1647 151 198 minecraft:air if block 1647 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1647 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1637 151 202 minecraft:air if block 1637 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1637 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1652 151 202 minecraft:air if block 1652 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1652 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1640 151 198 minecraft:air if block 1640 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1640 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1644 151 202 minecraft:air if block 1644 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1644 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1641 151 201 minecraft:air if block 1641 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1641 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1643 151 199 minecraft:air if block 1643 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1643 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 151 201 minecraft:air if block 1648 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1648 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1648 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1648 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b408 ftm_brick 2
