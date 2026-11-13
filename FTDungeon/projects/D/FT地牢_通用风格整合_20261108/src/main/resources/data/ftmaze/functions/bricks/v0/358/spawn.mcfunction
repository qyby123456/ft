scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 709 91 198 minecraft:air if block 709 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 709 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 699 91 202 minecraft:air if block 699 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 699 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 714 91 202 minecraft:air if block 714 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 714 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 91 198 minecraft:air if block 702 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 702 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 706 91 202 minecraft:air if block 706 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 706 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 703 91 201 minecraft:air if block 703 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 703 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 91 199 minecraft:air if block 705 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 705 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 710 91 201 minecraft:air if block 710 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 710 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 710 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 710 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b358 ftm_brick 2
