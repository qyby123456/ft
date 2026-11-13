scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 783 91 63 minecraft:air if block 783 92 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 783 91 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 91 77 minecraft:air if block 800 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 800 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 91 57 minecraft:air if block 800 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 800 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 91 77 minecraft:air if block 780 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 780 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 91 71 minecraft:air if block 790 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 790 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 91 66 minecraft:air if block 797 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 797 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 91 57 minecraft:air if block 789 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 789 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 780 91 57 minecraft:air if block 780 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 780 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 784 91 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 784 91 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b191 ftm_brick 2
