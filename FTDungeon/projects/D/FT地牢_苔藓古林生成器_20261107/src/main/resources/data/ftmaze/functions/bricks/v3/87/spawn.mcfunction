scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 31 175 minecraft:air if block 150 32 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 150 31 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 31 186 minecraft:air if block 166 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 166 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 31 186 minecraft:air if block 146 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 146 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 31 173 minecraft:air if block 163 32 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 163 31 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 31 179 minecraft:air if block 158 32 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 158 31 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 31 182 minecraft:air if block 151 32 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 151 31 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 31 172 minecraft:air if block 155 32 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 155 31 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 31 179 minecraft:air if block 147 32 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 147 31 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 31 176 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 31 176 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b87 ftm_brick 2
