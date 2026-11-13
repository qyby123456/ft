scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 937 31 198 minecraft:air if block 937 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 937 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 927 31 202 minecraft:air if block 927 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 927 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 942 31 202 minecraft:air if block 942 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 942 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 930 31 198 minecraft:air if block 930 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 930 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 934 31 202 minecraft:air if block 934 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 934 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 931 31 201 minecraft:air if block 931 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 931 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 933 31 199 minecraft:air if block 933 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 933 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 938 31 201 minecraft:air if block 938 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 938 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 938 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 938 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b355 ftm_brick 2
