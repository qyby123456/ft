scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 121 minecraft:air if block 206 212 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 211 132 minecraft:air if block 221 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 221 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 211 119 minecraft:air if block 219 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 219 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 211 132 minecraft:air if block 203 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 203 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 211 126 minecraft:air if block 214 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 214 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 211 128 minecraft:air if block 207 212 128 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 207 211 128 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 211 118 minecraft:air if block 211 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 211 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 211 132 minecraft:air if block 211 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 211 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 211 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 211 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b21 ftm_brick 2
