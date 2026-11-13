scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 783 211 63 minecraft:air if block 783 212 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 783 211 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 211 77 minecraft:air if block 800 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 800 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 211 57 minecraft:air if block 800 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 800 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 211 77 minecraft:air if block 780 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 780 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 211 71 minecraft:air if block 790 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 790 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 211 66 minecraft:air if block 797 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 797 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 211 57 minecraft:air if block 789 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 789 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 211 57 minecraft:air if block 780 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 780 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 784 211 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 784 211 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b191 ftm_brick 2
