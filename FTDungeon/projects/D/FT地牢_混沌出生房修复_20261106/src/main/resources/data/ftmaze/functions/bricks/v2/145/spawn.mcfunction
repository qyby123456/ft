scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 91 99 minecraft:air if block 22 92 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 22 91 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 91 110 minecraft:air if block 38 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 38 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 91 110 minecraft:air if block 18 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 18 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 91 97 minecraft:air if block 35 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 35 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 91 103 minecraft:air if block 30 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 30 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 91 106 minecraft:air if block 23 92 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 23 91 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 91 96 minecraft:air if block 27 92 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 27 91 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 91 103 minecraft:air if block 19 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 19 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 91 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 91 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b145 ftm_brick 2
