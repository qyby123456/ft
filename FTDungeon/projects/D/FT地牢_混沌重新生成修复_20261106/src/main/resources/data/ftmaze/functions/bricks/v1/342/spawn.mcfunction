scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 960 211 198 minecraft:air if block 960 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 960 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 950 211 202 minecraft:air if block 950 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 950 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 965 211 202 minecraft:air if block 965 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 965 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 953 211 198 minecraft:air if block 953 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 953 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 957 211 202 minecraft:air if block 957 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 957 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 954 211 201 minecraft:air if block 954 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 954 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 956 211 199 minecraft:air if block 956 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 956 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 961 211 201 minecraft:air if block 961 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 961 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 961 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 961 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b342 ftm_brick 2
