scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 702 91 34 minecraft:air if block 702 92 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 702 91 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 688 91 21 minecraft:air if block 688 92 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 688 91 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 91 18 minecraft:air if block 705 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 705 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 690 91 37 minecraft:air if block 690 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 690 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 694 91 28 minecraft:air if block 694 92 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 694 91 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 708 91 27 minecraft:air if block 708 92 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 708 91 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 696 91 19 minecraft:air if block 696 92 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 696 91 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 701 91 24 minecraft:air if block 701 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 701 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 703 91 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 703 91 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b186 ftm_brick 2
