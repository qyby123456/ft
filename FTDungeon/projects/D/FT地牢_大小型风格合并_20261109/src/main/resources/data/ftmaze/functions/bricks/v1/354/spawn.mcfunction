scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 895 31 198 minecraft:air if block 895 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 895 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 885 31 202 minecraft:air if block 885 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 885 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 31 202 minecraft:air if block 900 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 900 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 888 31 198 minecraft:air if block 888 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 888 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 892 31 202 minecraft:air if block 892 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 892 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 31 201 minecraft:air if block 889 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 889 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 891 31 199 minecraft:air if block 891 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 891 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 896 31 201 minecraft:air if block 896 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 896 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 896 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 896 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b354 ftm_brick 2
