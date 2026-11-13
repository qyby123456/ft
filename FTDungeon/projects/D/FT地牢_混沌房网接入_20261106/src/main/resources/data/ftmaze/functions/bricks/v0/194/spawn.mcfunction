scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 784 211 25 minecraft:air if block 784 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 784 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 211 38 minecraft:air if block 797 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 797 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 211 19 minecraft:air if block 798 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 798 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 211 35 minecraft:air if block 780 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 780 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 211 32 minecraft:air if block 790 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 790 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 211 29 minecraft:air if block 800 212 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 800 211 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 787 211 18 minecraft:air if block 787 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 787 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 792 211 23 minecraft:air if block 792 212 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 792 211 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 784 211 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 784 211 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b194 ftm_brick 2
