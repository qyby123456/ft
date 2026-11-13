scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 31 149 minecraft:air if block 178 32 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 178 31 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 31 160 minecraft:air if block 193 32 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 193 31 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 31 147 minecraft:air if block 191 32 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 191 31 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 31 160 minecraft:air if block 175 32 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 175 31 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 31 154 minecraft:air if block 186 32 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 186 31 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 31 156 minecraft:air if block 179 32 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 179 31 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 31 146 minecraft:air if block 183 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 183 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 31 160 minecraft:air if block 183 32 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 183 31 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 31 150 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 31 150 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b90 ftm_brick 2
