scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 151 150 minecraft:air if block 122 152 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 122 151 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 151 160 minecraft:air if block 133 152 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 133 151 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 151 146 minecraft:air if block 137 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 137 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 118 151 160 minecraft:air if block 118 152 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 118 151 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 151 144 minecraft:air if block 127 152 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 127 151 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 151 157 minecraft:air if block 125 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 125 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 151 152 minecraft:air if block 130 152 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 130 151 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 151 154 minecraft:air if block 136 152 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 136 151 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 151 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 151 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b34 ftm_brick 2
