scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 31 150 minecraft:air if block 122 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 122 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 31 160 minecraft:air if block 133 32 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 133 31 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 31 146 minecraft:air if block 137 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 137 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 118 31 160 minecraft:air if block 118 32 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 118 31 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 31 144 minecraft:air if block 127 32 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 127 31 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 31 157 minecraft:air if block 125 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 125 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 31 152 minecraft:air if block 130 32 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 130 31 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 31 154 minecraft:air if block 136 32 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 136 31 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 31 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 31 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b82 ftm_brick 2
