scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 993 211 198 minecraft:air if block 993 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 993 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 983 211 202 minecraft:air if block 983 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 983 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 998 211 202 minecraft:air if block 998 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 998 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 986 211 198 minecraft:air if block 986 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 986 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 990 211 202 minecraft:air if block 990 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 990 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 987 211 201 minecraft:air if block 987 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 987 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 211 199 minecraft:air if block 989 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 989 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 994 211 201 minecraft:air if block 994 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 994 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 994 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 994 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b379 ftm_brick 2
