scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 833 151 100 minecraft:air if block 833 152 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 833 151 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 151 115 minecraft:air if block 850 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 151 95 minecraft:air if block 849 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 849 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 115 minecraft:air if block 830 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 151 109 minecraft:air if block 840 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 840 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 151 101 minecraft:air if block 843 152 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 843 151 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 151 95 minecraft:air if block 838 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 838 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 151 106 minecraft:air if block 848 152 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 848 151 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 151 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 151 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b188 ftm_brick 2
