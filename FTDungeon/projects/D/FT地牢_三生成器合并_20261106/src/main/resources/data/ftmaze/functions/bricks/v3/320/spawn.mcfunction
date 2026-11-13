scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 708 91 198 minecraft:air if block 708 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 708 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 91 202 minecraft:air if block 698 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 698 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 713 91 202 minecraft:air if block 713 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 713 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 701 91 198 minecraft:air if block 701 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 701 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 91 202 minecraft:air if block 705 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 705 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 91 201 minecraft:air if block 702 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 702 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 704 91 199 minecraft:air if block 704 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 704 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 91 201 minecraft:air if block 709 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 709 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 709 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 709 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b320 ftm_brick 2
