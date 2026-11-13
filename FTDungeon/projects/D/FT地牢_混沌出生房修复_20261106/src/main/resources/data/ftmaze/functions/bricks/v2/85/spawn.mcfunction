scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 31 119 minecraft:air if block 150 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 150 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 31 130 minecraft:air if block 166 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 166 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 31 130 minecraft:air if block 146 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 146 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 31 117 minecraft:air if block 163 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 163 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 31 123 minecraft:air if block 158 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 158 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 31 126 minecraft:air if block 151 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 151 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 31 116 minecraft:air if block 155 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 155 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 31 123 minecraft:air if block 147 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 147 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 31 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 31 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b85 ftm_brick 2
