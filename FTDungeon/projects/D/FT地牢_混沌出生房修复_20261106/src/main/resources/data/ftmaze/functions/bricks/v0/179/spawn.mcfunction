scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 590 91 112 minecraft:air if block 590 92 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 590 91 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 607 91 126 minecraft:air if block 607 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 607 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 107 minecraft:air if block 605 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 91 126 minecraft:air if block 587 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 587 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 91 120 minecraft:air if block 597 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 597 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 595 91 107 minecraft:air if block 595 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 595 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 600 91 112 minecraft:air if block 600 92 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 600 91 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 117 minecraft:air if block 605 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 591 91 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 591 91 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b179 ftm_brick 2
