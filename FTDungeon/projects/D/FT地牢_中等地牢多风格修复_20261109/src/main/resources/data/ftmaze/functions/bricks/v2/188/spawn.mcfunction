scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 834 151 115 minecraft:air if block 834 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 834 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 151 129 minecraft:air if block 850 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 151 109 minecraft:air if block 849 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 849 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 129 minecraft:air if block 830 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 151 124 minecraft:air if block 840 152 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 840 151 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 151 118 minecraft:air if block 846 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 846 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 109 minecraft:air if block 830 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 151 110 minecraft:air if block 839 152 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 839 151 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 151 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 151 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b188 ftm_brick 2
