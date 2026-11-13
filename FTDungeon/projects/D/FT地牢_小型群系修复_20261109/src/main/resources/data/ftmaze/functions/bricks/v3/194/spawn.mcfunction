scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 784 91 25 minecraft:air if block 784 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 784 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 91 38 minecraft:air if block 797 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 797 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 91 19 minecraft:air if block 798 92 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 798 91 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 91 35 minecraft:air if block 780 92 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 780 91 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 91 32 minecraft:air if block 790 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 790 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 91 29 minecraft:air if block 800 92 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 800 91 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 787 91 18 minecraft:air if block 787 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 787 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 792 91 23 minecraft:air if block 792 92 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 792 91 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 784 91 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 784 91 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b194 ftm_brick 2
