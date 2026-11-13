scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 73 91 43 minecraft:air if block 73 92 43 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 73 91 43 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 91 33 minecraft:air if block 90 92 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 90 91 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 86 91 53 minecraft:air if block 86 92 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 86 91 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 33 minecraft:air if block 70 92 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 53 minecraft:air if block 70 92 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 91 36 minecraft:air if block 80 92 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 80 91 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 87 91 42 minecraft:air if block 87 92 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 87 91 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 91 49 minecraft:air if block 78 92 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 78 91 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 74 91 43 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 74 91 43 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b124 ftm_brick 2
