scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1059 31 198 minecraft:air if block 1059 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1059 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1049 31 202 minecraft:air if block 1049 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1049 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1064 31 202 minecraft:air if block 1064 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1064 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1052 31 198 minecraft:air if block 1052 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1052 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1056 31 202 minecraft:air if block 1056 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1056 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1053 31 201 minecraft:air if block 1053 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1053 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1055 31 199 minecraft:air if block 1055 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1055 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1060 31 201 minecraft:air if block 1060 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1060 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1060 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1060 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b394 ftm_brick 2
