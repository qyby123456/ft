scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1605 151 198 minecraft:air if block 1605 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1605 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1595 151 202 minecraft:air if block 1595 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1595 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1610 151 202 minecraft:air if block 1610 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1610 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 198 minecraft:air if block 1598 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1602 151 202 minecraft:air if block 1602 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1602 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1599 151 201 minecraft:air if block 1599 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1599 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1601 151 199 minecraft:air if block 1601 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1601 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 151 201 minecraft:air if block 1606 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1606 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1606 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1606 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b407 ftm_brick 2
