scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 783 151 63 minecraft:air if block 783 152 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 783 151 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 151 77 minecraft:air if block 800 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 800 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 151 57 minecraft:air if block 800 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 800 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 151 77 minecraft:air if block 780 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 780 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 151 71 minecraft:air if block 790 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 790 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 151 66 minecraft:air if block 797 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 797 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 151 57 minecraft:air if block 789 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 789 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 151 57 minecraft:air if block 780 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 780 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 784 151 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 784 151 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b191 ftm_brick 2
