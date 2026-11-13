scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1605 211 198 minecraft:air if block 1605 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1605 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1595 211 202 minecraft:air if block 1595 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1595 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1610 211 202 minecraft:air if block 1610 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1610 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 211 198 minecraft:air if block 1598 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1602 211 202 minecraft:air if block 1602 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1602 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1599 211 201 minecraft:air if block 1599 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1599 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1601 211 199 minecraft:air if block 1601 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1601 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 211 201 minecraft:air if block 1606 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1606 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1606 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1606 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b411 ftm_brick 2
