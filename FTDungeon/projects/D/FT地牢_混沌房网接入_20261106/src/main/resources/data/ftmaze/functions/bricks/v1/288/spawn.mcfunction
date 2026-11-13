scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 147 31 203 minecraft:air if block 147 32 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 147 31 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 140 31 212 minecraft:air if block 140 32 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 140 31 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 31 211 minecraft:air if block 151 32 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 151 31 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 140 31 204 minecraft:air if block 140 32 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 140 31 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 144 31 208 minecraft:air if block 144 32 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 144 31 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 145 31 213 minecraft:air if block 145 32 213 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 145 31 213 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 31 207 minecraft:air if block 149 32 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 149 31 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 139 31 208 minecraft:air if block 139 32 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 139 31 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 147 31 204 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 147 31 204 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b288 ftm_brick 2
