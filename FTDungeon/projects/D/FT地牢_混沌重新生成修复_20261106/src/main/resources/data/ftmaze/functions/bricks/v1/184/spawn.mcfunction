scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 592 91 25 minecraft:air if block 592 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 592 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 38 minecraft:air if block 605 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 91 19 minecraft:air if block 606 92 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 606 91 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 588 91 35 minecraft:air if block 588 92 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 588 91 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 598 91 32 minecraft:air if block 598 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 598 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 91 29 minecraft:air if block 608 92 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 608 91 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 595 91 18 minecraft:air if block 595 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 595 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 600 91 23 minecraft:air if block 600 92 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 600 91 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 592 91 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 592 91 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b184 ftm_brick 2
