scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 609 151 198 minecraft:air if block 609 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 609 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 599 151 202 minecraft:air if block 599 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 599 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 151 202 minecraft:air if block 614 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 614 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 602 151 198 minecraft:air if block 602 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 602 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 151 202 minecraft:air if block 606 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 606 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 603 151 201 minecraft:air if block 603 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 603 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 151 199 minecraft:air if block 605 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 605 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 610 151 201 minecraft:air if block 610 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 610 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 610 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 610 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b343 ftm_brick 2
