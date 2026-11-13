scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 784 151 25 minecraft:air if block 784 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 784 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 151 38 minecraft:air if block 797 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 797 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 151 19 minecraft:air if block 798 152 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 798 151 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 151 35 minecraft:air if block 780 152 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 780 151 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 151 32 minecraft:air if block 790 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 790 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 151 29 minecraft:air if block 800 152 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 800 151 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 787 151 18 minecraft:air if block 787 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 787 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 792 151 23 minecraft:air if block 792 152 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 792 151 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 784 151 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 784 151 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b194 ftm_brick 2
