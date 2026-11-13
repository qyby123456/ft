scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 633 93 34 minecraft:air if block 633 94 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 633 93 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 650 93 24 minecraft:air if block 650 94 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 650 93 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 93 44 minecraft:air if block 646 94 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 646 93 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 630 93 24 minecraft:air if block 630 94 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 630 93 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 630 93 44 minecraft:air if block 630 94 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 630 93 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 640 93 27 minecraft:air if block 640 94 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 640 93 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 647 93 33 minecraft:air if block 647 94 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 647 93 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 93 40 minecraft:air if block 638 94 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 93 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 634 93 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 634 93 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b185 ftm_brick 2
