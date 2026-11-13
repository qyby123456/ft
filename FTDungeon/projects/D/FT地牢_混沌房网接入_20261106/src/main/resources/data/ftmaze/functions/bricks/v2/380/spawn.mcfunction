scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 993 151 198 minecraft:air if block 993 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 993 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 983 151 202 minecraft:air if block 983 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 983 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 998 151 202 minecraft:air if block 998 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 998 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 986 151 198 minecraft:air if block 986 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 986 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 990 151 202 minecraft:air if block 990 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 990 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 987 151 201 minecraft:air if block 987 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 987 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 151 199 minecraft:air if block 989 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 989 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 994 151 201 minecraft:air if block 994 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 994 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 994 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 994 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b380 ftm_brick 2
