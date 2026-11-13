scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 211 150 minecraft:air if block 66 212 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 66 211 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 211 160 minecraft:air if block 77 212 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 77 211 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 211 146 minecraft:air if block 81 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 81 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 62 211 160 minecraft:air if block 62 212 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 62 211 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 211 144 minecraft:air if block 71 212 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 71 211 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 69 211 157 minecraft:air if block 69 212 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 69 211 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 211 152 minecraft:air if block 74 212 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 74 211 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 211 154 minecraft:air if block 80 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 80 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 211 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 211 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b2 ftm_brick 2
