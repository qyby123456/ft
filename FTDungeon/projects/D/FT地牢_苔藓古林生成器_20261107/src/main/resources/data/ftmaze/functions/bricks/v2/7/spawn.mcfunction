scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 105 211 177 minecraft:air if block 105 212 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 105 211 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 211 184 minecraft:air if block 91 212 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 91 211 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 211 174 minecraft:air if block 94 212 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 94 211 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 211 186 minecraft:air if block 109 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 109 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 211 180 minecraft:air if block 98 212 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 98 211 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 101 211 172 minecraft:air if block 101 212 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 101 211 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 211 186 minecraft:air if block 100 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 100 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 211 182 minecraft:air if block 103 212 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 103 211 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 106 211 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 106 211 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b7 ftm_brick 2
