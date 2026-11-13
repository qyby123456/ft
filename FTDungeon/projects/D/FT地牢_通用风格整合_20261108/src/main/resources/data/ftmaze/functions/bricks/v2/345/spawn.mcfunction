scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 643 91 198 minecraft:air if block 643 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 643 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 633 91 202 minecraft:air if block 633 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 633 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 91 202 minecraft:air if block 648 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 648 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 636 91 198 minecraft:air if block 636 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 636 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 640 91 202 minecraft:air if block 640 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 640 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 637 91 201 minecraft:air if block 637 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 637 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 639 91 199 minecraft:air if block 639 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 639 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 644 91 201 minecraft:air if block 644 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 644 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 644 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 644 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b345 ftm_brick 2
