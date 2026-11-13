scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 993 91 198 minecraft:air if block 993 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 993 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 983 91 202 minecraft:air if block 983 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 983 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 998 91 202 minecraft:air if block 998 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 998 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 986 91 198 minecraft:air if block 986 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 986 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 990 91 202 minecraft:air if block 990 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 990 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 987 91 201 minecraft:air if block 987 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 987 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 91 199 minecraft:air if block 989 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 989 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 994 91 201 minecraft:air if block 994 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 994 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 994 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 994 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b379 ftm_brick 2
