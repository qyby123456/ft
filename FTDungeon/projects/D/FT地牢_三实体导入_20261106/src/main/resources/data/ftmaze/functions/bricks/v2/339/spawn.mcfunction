scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 960 31 198 minecraft:air if block 960 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 960 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 950 31 202 minecraft:air if block 950 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 950 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 965 31 202 minecraft:air if block 965 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 965 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 953 31 198 minecraft:air if block 953 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 953 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 957 31 202 minecraft:air if block 957 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 957 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 954 31 201 minecraft:air if block 954 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 954 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 956 31 199 minecraft:air if block 956 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 956 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 961 31 201 minecraft:air if block 961 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 961 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 961 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 961 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b339 ftm_brick 2
