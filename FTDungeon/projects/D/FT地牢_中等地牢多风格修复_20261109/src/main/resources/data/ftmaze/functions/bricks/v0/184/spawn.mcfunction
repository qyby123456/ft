scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 590 91 31 minecraft:air if block 590 92 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 590 91 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 607 91 44 minecraft:air if block 607 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 607 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 25 minecraft:air if block 605 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 91 44 minecraft:air if block 587 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 587 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 91 39 minecraft:air if block 597 92 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 597 91 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 601 91 32 minecraft:air if block 601 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 601 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 91 25 minecraft:air if block 594 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 594 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 37 minecraft:air if block 605 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 591 91 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 591 91 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b184 ftm_brick 2
