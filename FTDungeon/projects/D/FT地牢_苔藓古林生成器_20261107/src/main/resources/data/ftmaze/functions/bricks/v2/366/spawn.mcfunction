scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 927 211 198 minecraft:air if block 927 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 927 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 917 211 202 minecraft:air if block 917 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 917 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 932 211 202 minecraft:air if block 932 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 932 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 920 211 198 minecraft:air if block 920 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 920 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 924 211 202 minecraft:air if block 924 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 924 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 921 211 201 minecraft:air if block 921 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 921 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 923 211 199 minecraft:air if block 923 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 923 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 928 211 201 minecraft:air if block 928 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 928 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 928 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 928 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b366 ftm_brick 2
