scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1689 211 198 minecraft:air if block 1689 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1689 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1679 211 202 minecraft:air if block 1679 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1679 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1694 211 202 minecraft:air if block 1694 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1694 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1682 211 198 minecraft:air if block 1682 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1682 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1686 211 202 minecraft:air if block 1686 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1686 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1683 211 201 minecraft:air if block 1683 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1683 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1685 211 199 minecraft:air if block 1685 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1685 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1690 211 201 minecraft:air if block 1690 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1690 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1690 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1690 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b413 ftm_brick 2
