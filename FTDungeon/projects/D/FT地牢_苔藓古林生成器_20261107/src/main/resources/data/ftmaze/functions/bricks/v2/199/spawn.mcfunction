scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 976 31 119 minecraft:air if block 976 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 976 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 31 109 minecraft:air if block 992 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 992 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 31 129 minecraft:air if block 989 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 989 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 979 31 109 minecraft:air if block 979 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 979 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 974 31 129 minecraft:air if block 974 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 974 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 987 31 118 minecraft:air if block 987 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 987 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 981 31 125 minecraft:air if block 981 32 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 981 31 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 31 122 minecraft:air if block 992 32 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 992 31 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 977 31 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 977 31 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b199 ftm_brick 2
