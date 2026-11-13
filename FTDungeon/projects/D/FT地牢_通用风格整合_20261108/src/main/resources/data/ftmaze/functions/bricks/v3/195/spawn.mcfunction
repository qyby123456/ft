scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 833 95 29 minecraft:air if block 833 96 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 833 95 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 95 19 minecraft:air if block 850 96 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 850 95 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 95 39 minecraft:air if block 846 96 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 846 95 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 95 19 minecraft:air if block 830 96 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 95 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 95 39 minecraft:air if block 830 96 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 95 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 95 22 minecraft:air if block 840 96 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 840 95 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 847 95 28 minecraft:air if block 847 96 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 847 95 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 95 35 minecraft:air if block 838 96 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 838 95 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 95 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 95 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b195 ftm_brick 2
