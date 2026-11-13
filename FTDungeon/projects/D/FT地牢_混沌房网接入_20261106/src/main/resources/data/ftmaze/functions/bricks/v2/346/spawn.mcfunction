scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 685 91 198 minecraft:air if block 685 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 685 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 675 91 202 minecraft:air if block 675 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 675 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 690 91 202 minecraft:air if block 690 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 690 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 678 91 198 minecraft:air if block 678 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 678 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 682 91 202 minecraft:air if block 682 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 682 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 679 91 201 minecraft:air if block 679 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 679 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 681 91 199 minecraft:air if block 681 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 681 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 686 91 201 minecraft:air if block 686 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 686 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 686 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 686 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b346 ftm_brick 2
