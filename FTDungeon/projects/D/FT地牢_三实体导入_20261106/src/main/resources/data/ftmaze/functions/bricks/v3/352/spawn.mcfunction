scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 937 211 198 minecraft:air if block 937 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 937 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 927 211 202 minecraft:air if block 927 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 927 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 942 211 202 minecraft:air if block 942 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 942 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 930 211 198 minecraft:air if block 930 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 930 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 934 211 202 minecraft:air if block 934 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 934 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 931 211 201 minecraft:air if block 931 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 931 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 933 211 199 minecraft:air if block 933 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 933 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 938 211 201 minecraft:air if block 938 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 938 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 938 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 938 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b352 ftm_brick 2
