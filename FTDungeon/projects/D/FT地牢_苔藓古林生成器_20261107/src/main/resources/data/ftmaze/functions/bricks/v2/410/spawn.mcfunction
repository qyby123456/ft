scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1647 31 198 minecraft:air if block 1647 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1647 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1637 31 202 minecraft:air if block 1637 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1637 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1652 31 202 minecraft:air if block 1652 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1652 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1640 31 198 minecraft:air if block 1640 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1640 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1644 31 202 minecraft:air if block 1644 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1644 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1641 31 201 minecraft:air if block 1641 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1641 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1643 31 199 minecraft:air if block 1643 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1643 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 31 201 minecraft:air if block 1648 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1648 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1648 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1648 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b410 ftm_brick 2
