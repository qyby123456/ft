scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 151 31 177 minecraft:air if block 151 32 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 151 31 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 31 187 minecraft:air if block 162 32 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 162 31 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 31 173 minecraft:air if block 166 32 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 166 31 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 31 187 minecraft:air if block 149 32 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 149 31 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 31 183 minecraft:air if block 155 32 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 155 31 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 31 172 minecraft:air if block 156 32 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 156 31 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 31 178 minecraft:air if block 160 32 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 160 31 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 31 173 minecraft:air if block 146 32 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 146 31 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 31 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 31 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b87 ftm_brick 2
