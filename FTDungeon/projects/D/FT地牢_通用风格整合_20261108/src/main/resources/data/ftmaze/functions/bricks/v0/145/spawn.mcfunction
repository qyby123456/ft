scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 91 100 minecraft:air if block 22 92 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 22 91 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 91 110 minecraft:air if block 35 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 35 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 91 97 minecraft:air if block 37 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 37 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 91 108 minecraft:air if block 19 92 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 19 91 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 91 103 minecraft:air if block 30 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 30 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 91 96 minecraft:air if block 27 92 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 27 91 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 24 91 105 minecraft:air if block 24 92 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 24 91 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 91 104 minecraft:air if block 36 92 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 36 91 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 91 101 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 91 101 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b145 ftm_brick 2
