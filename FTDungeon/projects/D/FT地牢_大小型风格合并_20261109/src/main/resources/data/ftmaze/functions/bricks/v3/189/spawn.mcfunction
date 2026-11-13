scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 784 91 119 minecraft:air if block 784 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 784 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 91 109 minecraft:air if block 800 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 800 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 91 129 minecraft:air if block 797 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 797 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 787 91 109 minecraft:air if block 787 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 787 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 91 129 minecraft:air if block 782 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 782 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 795 91 118 minecraft:air if block 795 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 795 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 91 125 minecraft:air if block 789 92 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 789 91 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 91 122 minecraft:air if block 800 92 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 800 91 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 785 91 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 785 91 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b189 ftm_brick 2
