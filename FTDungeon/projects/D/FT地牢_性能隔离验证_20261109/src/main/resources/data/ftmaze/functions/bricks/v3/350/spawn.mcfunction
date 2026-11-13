scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 853 211 198 minecraft:air if block 853 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 853 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 211 202 minecraft:air if block 843 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 843 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 858 211 202 minecraft:air if block 858 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 858 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 211 198 minecraft:air if block 846 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 846 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 211 202 minecraft:air if block 850 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 850 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 847 211 201 minecraft:air if block 847 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 847 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 211 199 minecraft:air if block 849 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 849 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 854 211 201 minecraft:air if block 854 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 854 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 854 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 854 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b350 ftm_brick 2
