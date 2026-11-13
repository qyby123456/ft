scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 833 155 29 minecraft:air if block 833 156 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 833 155 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 155 19 minecraft:air if block 850 156 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 155 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 155 39 minecraft:air if block 846 156 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 846 155 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 155 19 minecraft:air if block 830 156 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 155 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 155 39 minecraft:air if block 830 156 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 155 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 155 22 minecraft:air if block 840 156 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 840 155 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 847 155 28 minecraft:air if block 847 156 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 847 155 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 155 35 minecraft:air if block 838 156 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 838 155 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 155 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 155 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b195 ftm_brick 2
