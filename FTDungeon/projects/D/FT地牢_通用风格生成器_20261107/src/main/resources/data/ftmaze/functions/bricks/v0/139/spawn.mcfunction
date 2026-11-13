scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 211 99 minecraft:air if block 27 212 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 27 211 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 211 116 minecraft:air if block 38 212 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 38 211 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 211 113 minecraft:air if block 18 212 113 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 18 211 113 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 211 108 minecraft:air if block 32 212 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 32 211 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 211 102 minecraft:air if block 38 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 38 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 211 102 minecraft:air if block 18 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 18 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 211 107 minecraft:air if block 23 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 23 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 211 105 minecraft:air if block 28 212 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 28 211 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 27 211 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 27 211 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b139 ftm_brick 2
