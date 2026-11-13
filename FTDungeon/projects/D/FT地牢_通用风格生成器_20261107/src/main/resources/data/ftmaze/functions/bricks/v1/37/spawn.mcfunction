scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 151 119 minecraft:air if block 150 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 150 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 151 130 minecraft:air if block 166 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 166 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 151 130 minecraft:air if block 146 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 146 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 151 117 minecraft:air if block 163 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 163 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 151 123 minecraft:air if block 158 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 158 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 151 126 minecraft:air if block 151 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 151 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 116 minecraft:air if block 155 152 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 151 123 minecraft:air if block 147 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 147 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 151 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 151 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b37 ftm_brick 2
