scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 843 211 198 minecraft:air if block 843 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 843 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 833 211 202 minecraft:air if block 833 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 833 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 211 202 minecraft:air if block 848 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 848 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 836 211 198 minecraft:air if block 836 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 836 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 211 202 minecraft:air if block 840 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 840 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 211 201 minecraft:air if block 837 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 837 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 211 199 minecraft:air if block 839 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 839 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 844 211 201 minecraft:air if block 844 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 844 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 844 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 844 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b364 ftm_brick 2
