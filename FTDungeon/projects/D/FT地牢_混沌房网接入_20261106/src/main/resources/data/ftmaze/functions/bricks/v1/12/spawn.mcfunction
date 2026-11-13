scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 149 211 96 minecraft:air if block 149 212 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 149 211 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 211 105 minecraft:air if block 163 212 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 163 211 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 211 93 minecraft:air if block 160 212 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 160 211 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 153 211 103 minecraft:air if block 153 212 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 153 211 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 96 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 96 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b12 ftm_brick 2
