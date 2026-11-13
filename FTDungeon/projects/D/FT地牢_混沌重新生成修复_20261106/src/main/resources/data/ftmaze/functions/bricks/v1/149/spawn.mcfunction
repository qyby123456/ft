scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 31 147 minecraft:air if block 27 32 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 27 31 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 31 164 minecraft:air if block 38 32 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 38 31 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 31 161 minecraft:air if block 18 32 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 18 31 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 31 156 minecraft:air if block 32 32 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 32 31 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 31 150 minecraft:air if block 38 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 38 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 31 150 minecraft:air if block 18 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 18 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 31 155 minecraft:air if block 23 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 23 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 31 153 minecraft:air if block 28 32 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 28 31 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 27 31 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 27 31 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b149 ftm_brick 2
