scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 73 91 36 minecraft:air if block 73 92 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 73 91 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 91 51 minecraft:air if block 90 92 51 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 90 91 51 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 89 91 31 minecraft:air if block 89 92 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 89 91 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 51 minecraft:air if block 70 92 51 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 51 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 91 45 minecraft:air if block 80 92 45 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 80 91 45 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 83 91 37 minecraft:air if block 83 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 83 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 91 31 minecraft:air if block 78 92 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 78 91 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 91 40 minecraft:air if block 90 92 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 90 91 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 74 91 36 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 74 91 36 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b126 ftm_brick 2
