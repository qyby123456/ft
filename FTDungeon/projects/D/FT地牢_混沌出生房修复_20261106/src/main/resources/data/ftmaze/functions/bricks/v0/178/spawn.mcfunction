scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 633 91 117 minecraft:air if block 633 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 633 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 650 91 107 minecraft:air if block 650 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 650 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 91 127 minecraft:air if block 646 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 646 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 630 91 107 minecraft:air if block 630 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 630 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 630 91 127 minecraft:air if block 630 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 630 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 640 91 110 minecraft:air if block 640 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 640 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 647 91 116 minecraft:air if block 647 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 647 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 123 minecraft:air if block 638 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 634 91 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 634 91 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b178 ftm_brick 2
