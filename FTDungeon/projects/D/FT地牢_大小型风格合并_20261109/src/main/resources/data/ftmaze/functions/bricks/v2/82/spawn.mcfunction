scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 126 31 158 minecraft:air if block 126 32 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 126 31 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 31 155 minecraft:air if block 137 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 137 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 31 166 minecraft:air if block 122 32 166 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 122 31 166 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 31 164 minecraft:air if block 131 32 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 131 31 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 31 155 minecraft:air if block 119 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 119 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 31 154 minecraft:air if block 130 32 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 130 31 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 31 163 minecraft:air if block 125 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 125 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 31 157 minecraft:air if block 133 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 133 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 127 31 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 127 31 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b82 ftm_brick 2
