scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 31 149 minecraft:air if block 179 32 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 179 31 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 31 159 minecraft:air if block 190 32 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 190 31 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 31 145 minecraft:air if block 194 32 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 194 31 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 31 159 minecraft:air if block 177 32 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 177 31 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 31 155 minecraft:air if block 183 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 183 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 31 144 minecraft:air if block 184 32 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 184 31 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 31 150 minecraft:air if block 188 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 188 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 31 145 minecraft:air if block 174 32 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 174 31 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 31 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 31 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b90 ftm_brick 2
