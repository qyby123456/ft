scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 31 151 minecraft:air if block 178 32 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 178 31 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 31 158 minecraft:air if block 194 32 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 194 31 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 31 146 minecraft:air if block 189 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 189 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 31 159 minecraft:air if block 182 32 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 182 31 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 31 157 minecraft:air if block 174 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 174 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 31 153 minecraft:air if block 186 32 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 186 31 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 31 146 minecraft:air if block 174 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 174 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 31 147 minecraft:air if block 182 32 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 182 31 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 31 150 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 31 150 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b90 ftm_brick 2
