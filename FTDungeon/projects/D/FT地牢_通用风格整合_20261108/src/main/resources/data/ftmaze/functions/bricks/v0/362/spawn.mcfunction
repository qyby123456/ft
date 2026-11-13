scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 877 91 198 minecraft:air if block 877 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 877 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 867 91 202 minecraft:air if block 867 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 867 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 882 91 202 minecraft:air if block 882 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 882 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 870 91 198 minecraft:air if block 870 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 870 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 874 91 202 minecraft:air if block 874 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 874 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 871 91 201 minecraft:air if block 871 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 871 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 873 91 199 minecraft:air if block 873 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 873 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 878 91 201 minecraft:air if block 878 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 878 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 878 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 878 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b362 ftm_brick 2
