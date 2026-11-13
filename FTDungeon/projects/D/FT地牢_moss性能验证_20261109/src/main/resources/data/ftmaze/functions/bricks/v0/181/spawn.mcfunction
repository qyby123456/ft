scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 590 91 71 minecraft:air if block 590 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 590 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 607 91 86 minecraft:air if block 607 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 607 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 91 66 minecraft:air if block 606 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 606 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 91 86 minecraft:air if block 587 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 587 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 91 80 minecraft:air if block 597 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 597 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 600 91 72 minecraft:air if block 600 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 600 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 595 91 66 minecraft:air if block 595 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 595 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 77 minecraft:air if block 605 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 591 91 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 591 91 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b181 ftm_brick 2
