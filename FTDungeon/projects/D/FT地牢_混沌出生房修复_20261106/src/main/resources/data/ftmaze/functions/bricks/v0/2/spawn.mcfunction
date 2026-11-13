scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 211 150 minecraft:air if block 66 212 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 66 211 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 211 160 minecraft:air if block 79 212 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 79 211 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 211 147 minecraft:air if block 81 212 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 81 211 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 211 158 minecraft:air if block 63 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 63 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 211 153 minecraft:air if block 74 212 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 74 211 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 211 146 minecraft:air if block 71 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 71 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 68 211 155 minecraft:air if block 68 212 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 68 211 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 211 154 minecraft:air if block 80 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 80 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 211 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 211 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b2 ftm_brick 2
