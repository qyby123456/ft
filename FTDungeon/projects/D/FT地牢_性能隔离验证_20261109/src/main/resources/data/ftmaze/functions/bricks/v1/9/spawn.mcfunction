scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 211 125 minecraft:air if block 122 212 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 122 211 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 211 134 minecraft:air if block 134 212 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 134 211 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 132 211 121 minecraft:air if block 132 212 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 132 211 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 123 211 134 minecraft:air if block 123 212 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 123 211 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 211 124 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 211 124 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b9 ftm_brick 2
