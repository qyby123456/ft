scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1605 31 198 minecraft:air if block 1605 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1605 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1595 31 202 minecraft:air if block 1595 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1595 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1610 31 202 minecraft:air if block 1610 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1610 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 31 198 minecraft:air if block 1598 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1598 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1602 31 202 minecraft:air if block 1602 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1602 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1599 31 201 minecraft:air if block 1599 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1599 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1601 31 199 minecraft:air if block 1601 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1601 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 31 201 minecraft:air if block 1606 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1606 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1606 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1606 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b409 ftm_brick 2
