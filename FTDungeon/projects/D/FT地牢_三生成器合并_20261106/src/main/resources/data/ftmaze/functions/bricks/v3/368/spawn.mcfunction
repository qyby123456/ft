scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1053 31 198 minecraft:air if block 1053 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1053 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1043 31 202 minecraft:air if block 1043 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1043 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1058 31 202 minecraft:air if block 1058 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1058 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1046 31 198 minecraft:air if block 1046 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1046 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1050 31 202 minecraft:air if block 1050 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1050 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1047 31 201 minecraft:air if block 1047 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1047 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1049 31 199 minecraft:air if block 1049 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1049 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1054 31 201 minecraft:air if block 1054 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1054 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1054 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1054 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b368 ftm_brick 2
