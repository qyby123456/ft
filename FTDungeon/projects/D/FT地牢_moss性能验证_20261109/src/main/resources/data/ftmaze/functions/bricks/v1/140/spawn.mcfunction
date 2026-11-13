scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 211 147 minecraft:air if block 22 212 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 22 211 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 211 158 minecraft:air if block 38 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 38 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 211 158 minecraft:air if block 18 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 18 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 211 145 minecraft:air if block 35 212 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 35 211 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 211 151 minecraft:air if block 30 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 30 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 211 154 minecraft:air if block 23 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 23 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 211 144 minecraft:air if block 27 212 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 27 211 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 211 151 minecraft:air if block 19 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 19 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 211 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 211 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b140 ftm_brick 2
