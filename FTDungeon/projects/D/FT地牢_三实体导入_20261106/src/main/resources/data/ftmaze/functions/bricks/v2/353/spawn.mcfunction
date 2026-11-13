scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 979 91 198 minecraft:air if block 979 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 979 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 969 91 202 minecraft:air if block 969 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 969 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 984 91 202 minecraft:air if block 984 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 984 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 91 198 minecraft:air if block 972 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 972 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 976 91 202 minecraft:air if block 976 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 976 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 973 91 201 minecraft:air if block 973 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 973 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 975 91 199 minecraft:air if block 975 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 975 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 980 91 201 minecraft:air if block 980 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 980 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 980 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 980 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b353 ftm_brick 2
