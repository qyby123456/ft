scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 927 31 198 minecraft:air if block 927 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 927 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 917 31 202 minecraft:air if block 917 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 917 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 932 31 202 minecraft:air if block 932 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 932 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 920 31 198 minecraft:air if block 920 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 920 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 924 31 202 minecraft:air if block 924 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 924 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 921 31 201 minecraft:air if block 921 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 921 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 923 31 199 minecraft:air if block 923 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 923 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 928 31 201 minecraft:air if block 928 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 928 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 928 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 928 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b365 ftm_brick 2
