scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 183 31 98 minecraft:air if block 183 32 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 183 31 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 31 89 minecraft:air if block 194 32 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 194 31 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 31 89 minecraft:air if block 174 32 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 174 31 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 31 102 minecraft:air if block 193 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 193 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 31 102 minecraft:air if block 175 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 175 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 31 88 minecraft:air if block 184 32 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 184 31 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 31 94 minecraft:air if block 189 32 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 189 31 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 31 95 minecraft:air if block 177 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 177 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 184 31 98 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 184 31 98 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b88 ftm_brick 2
