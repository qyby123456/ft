scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 835 151 76 minecraft:air if block 835 152 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 835 151 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 151 88 minecraft:air if block 850 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 151 68 minecraft:air if block 849 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 849 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 88 minecraft:air if block 830 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 68 minecraft:air if block 830 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 151 81 minecraft:air if block 843 152 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 843 151 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 151 85 minecraft:air if block 837 152 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 837 151 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 151 69 minecraft:air if block 838 152 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 838 151 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 151 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 151 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b192 ftm_brick 2
