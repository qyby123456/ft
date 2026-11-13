scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 633 91 71 minecraft:air if block 633 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 633 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 650 91 86 minecraft:air if block 650 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 650 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 649 91 66 minecraft:air if block 649 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 649 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 630 91 86 minecraft:air if block 630 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 630 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 640 91 80 minecraft:air if block 640 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 640 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 643 91 72 minecraft:air if block 643 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 643 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 66 minecraft:air if block 638 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 91 77 minecraft:air if block 648 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 648 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 634 91 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 634 91 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b182 ftm_brick 2
