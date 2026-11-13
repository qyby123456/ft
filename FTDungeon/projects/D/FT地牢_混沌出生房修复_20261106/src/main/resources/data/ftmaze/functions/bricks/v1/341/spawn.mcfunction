scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 918 211 198 minecraft:air if block 918 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 918 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 908 211 202 minecraft:air if block 908 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 908 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 923 211 202 minecraft:air if block 923 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 923 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 911 211 198 minecraft:air if block 911 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 911 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 915 211 202 minecraft:air if block 915 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 915 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 912 211 201 minecraft:air if block 912 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 912 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 914 211 199 minecraft:air if block 914 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 914 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 919 211 201 minecraft:air if block 919 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 919 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 919 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 919 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b341 ftm_brick 2
