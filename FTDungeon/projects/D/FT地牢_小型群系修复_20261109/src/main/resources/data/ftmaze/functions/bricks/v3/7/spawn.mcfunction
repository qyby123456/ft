scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 97 212 186 minecraft:air if block 97 213 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 97 212 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 212 175 minecraft:air if block 109 213 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 109 212 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 212 175 minecraft:air if block 91 213 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 91 212 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 212 190 minecraft:air if block 108 213 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 108 212 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 212 177 minecraft:air if block 100 213 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 100 212 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 212 190 minecraft:air if block 91 213 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 91 212 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 212 183 minecraft:air if block 103 213 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 103 212 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 92 212 182 minecraft:air if block 92 213 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 92 212 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 98 212 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 98 212 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b7 ftm_brick 2
