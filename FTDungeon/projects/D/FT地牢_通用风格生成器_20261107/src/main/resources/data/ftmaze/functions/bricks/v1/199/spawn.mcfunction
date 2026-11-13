scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 976 211 119 minecraft:air if block 976 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 976 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 211 109 minecraft:air if block 992 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 992 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 211 129 minecraft:air if block 989 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 989 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 979 211 109 minecraft:air if block 979 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 979 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 974 211 129 minecraft:air if block 974 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 974 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 987 211 118 minecraft:air if block 987 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 987 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 981 211 125 minecraft:air if block 981 212 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 981 211 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 211 122 minecraft:air if block 992 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 992 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 977 211 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 977 211 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b199 ftm_brick 2
