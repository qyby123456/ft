scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 811 211 198 minecraft:air if block 811 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 811 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 801 211 202 minecraft:air if block 801 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 801 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 816 211 202 minecraft:air if block 816 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 816 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 804 211 198 minecraft:air if block 804 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 804 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 808 211 202 minecraft:air if block 808 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 808 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 805 211 201 minecraft:air if block 805 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 805 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 807 211 199 minecraft:air if block 807 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 807 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 812 211 201 minecraft:air if block 812 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 812 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 812 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 812 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b349 ftm_brick 2
