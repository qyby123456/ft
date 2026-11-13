scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 183 151 154 minecraft:air if block 183 152 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 183 151 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 151 145 minecraft:air if block 194 152 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 194 151 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 145 minecraft:air if block 174 152 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 151 158 minecraft:air if block 193 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 193 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 151 158 minecraft:air if block 175 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 175 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 151 144 minecraft:air if block 184 152 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 184 151 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 151 150 minecraft:air if block 189 152 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 189 151 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 151 151 minecraft:air if block 177 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 177 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 184 151 154 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 184 151 154 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b42 ftm_brick 2
