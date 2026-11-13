scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1647 211 198 minecraft:air if block 1647 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1647 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1637 211 202 minecraft:air if block 1637 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1637 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1652 211 202 minecraft:air if block 1652 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1652 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1640 211 198 minecraft:air if block 1640 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1640 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1644 211 202 minecraft:air if block 1644 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1644 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1641 211 201 minecraft:air if block 1641 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1641 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1643 211 199 minecraft:air if block 1643 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1643 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 211 201 minecraft:air if block 1648 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1648 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1648 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1648 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b412 ftm_brick 2
