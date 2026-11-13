scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 125 32 158 minecraft:air if block 125 33 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 125 32 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 32 147 minecraft:air if block 137 33 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 137 32 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 32 147 minecraft:air if block 119 33 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 119 32 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 32 162 minecraft:air if block 136 33 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 136 32 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 32 149 minecraft:air if block 128 33 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 128 32 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 32 162 minecraft:air if block 119 33 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 119 32 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 32 155 minecraft:air if block 131 33 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 131 32 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 32 154 minecraft:air if block 120 33 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 120 32 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 126 32 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 126 32 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b82 ftm_brick 2
