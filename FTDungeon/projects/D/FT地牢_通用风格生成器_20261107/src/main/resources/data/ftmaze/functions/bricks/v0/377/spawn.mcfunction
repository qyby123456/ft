scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 909 211 198 minecraft:air if block 909 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 909 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 899 211 202 minecraft:air if block 899 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 899 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 914 211 202 minecraft:air if block 914 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 914 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 902 211 198 minecraft:air if block 902 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 902 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 906 211 202 minecraft:air if block 906 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 906 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 903 211 201 minecraft:air if block 903 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 903 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 905 211 199 minecraft:air if block 905 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 905 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 910 211 201 minecraft:air if block 910 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 910 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 910 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 910 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b377 ftm_brick 2
