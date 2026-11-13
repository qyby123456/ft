scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 601 91 198 minecraft:air if block 601 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 601 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 591 91 202 minecraft:air if block 591 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 591 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 91 202 minecraft:air if block 606 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 606 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 91 198 minecraft:air if block 594 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 594 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 598 91 202 minecraft:air if block 598 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 598 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 595 91 201 minecraft:air if block 595 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 595 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 91 199 minecraft:air if block 597 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 597 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 602 91 201 minecraft:air if block 602 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 602 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 602 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 602 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b344 ftm_brick 2
