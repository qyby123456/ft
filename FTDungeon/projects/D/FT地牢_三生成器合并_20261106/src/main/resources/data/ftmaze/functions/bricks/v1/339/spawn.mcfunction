scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 834 211 198 minecraft:air if block 834 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 834 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 824 211 202 minecraft:air if block 824 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 824 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 211 202 minecraft:air if block 839 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 839 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 827 211 198 minecraft:air if block 827 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 827 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 831 211 202 minecraft:air if block 831 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 831 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 828 211 201 minecraft:air if block 828 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 828 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 211 199 minecraft:air if block 830 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 835 211 201 minecraft:air if block 835 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 835 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 835 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 835 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b339 ftm_brick 2
