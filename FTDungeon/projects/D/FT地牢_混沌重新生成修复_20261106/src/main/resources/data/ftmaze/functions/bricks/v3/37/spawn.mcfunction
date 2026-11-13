scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 151 121 minecraft:air if block 150 152 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 150 151 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 151 132 minecraft:air if block 165 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 165 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 151 119 minecraft:air if block 163 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 163 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 151 132 minecraft:air if block 147 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 147 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 151 126 minecraft:air if block 158 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 158 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 151 128 minecraft:air if block 151 152 128 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 151 151 128 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 118 minecraft:air if block 155 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 132 minecraft:air if block 155 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 151 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 151 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b37 ftm_brick 2
