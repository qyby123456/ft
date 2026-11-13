scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 126 151 158 minecraft:air if block 126 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 126 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 151 155 minecraft:air if block 137 152 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 137 151 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 151 166 minecraft:air if block 122 152 166 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 122 151 166 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 151 164 minecraft:air if block 131 152 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 131 151 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 151 155 minecraft:air if block 119 152 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 119 151 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 151 154 minecraft:air if block 130 152 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 130 151 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 151 163 minecraft:air if block 125 152 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 125 151 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 151 157 minecraft:air if block 133 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 133 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 127 151 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 127 151 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b34 ftm_brick 2
