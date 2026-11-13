scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 784 151 119 minecraft:air if block 784 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 784 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 151 109 minecraft:air if block 800 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 800 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 151 129 minecraft:air if block 797 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 797 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 787 151 109 minecraft:air if block 787 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 787 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 151 129 minecraft:air if block 782 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 782 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 795 151 118 minecraft:air if block 795 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 795 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 151 125 minecraft:air if block 789 152 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 789 151 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 151 122 minecraft:air if block 800 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 800 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 785 151 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 785 151 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b189 ftm_brick 2
