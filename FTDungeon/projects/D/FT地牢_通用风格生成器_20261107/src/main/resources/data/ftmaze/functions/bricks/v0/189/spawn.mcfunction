scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 784 211 119 minecraft:air if block 784 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 784 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 211 109 minecraft:air if block 800 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 800 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 211 129 minecraft:air if block 797 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 797 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 787 211 109 minecraft:air if block 787 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 787 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 211 129 minecraft:air if block 782 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 782 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 795 211 118 minecraft:air if block 795 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 795 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 211 125 minecraft:air if block 789 212 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 789 211 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 211 122 minecraft:air if block 800 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 800 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 785 211 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 785 211 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b189 ftm_brick 2
