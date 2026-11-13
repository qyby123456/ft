scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 691 91 115 minecraft:air if block 691 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 691 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 706 91 129 minecraft:air if block 706 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 706 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 91 109 minecraft:air if block 705 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 705 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 688 91 129 minecraft:air if block 688 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 688 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 697 91 123 minecraft:air if block 697 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 697 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 91 117 minecraft:air if block 702 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 702 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 695 91 109 minecraft:air if block 695 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 695 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 687 91 120 minecraft:air if block 687 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 687 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 691 91 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 691 91 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b180 ftm_brick 2
