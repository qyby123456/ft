scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 31 121 minecraft:air if block 150 32 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 150 31 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 31 132 minecraft:air if block 165 32 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 165 31 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 31 119 minecraft:air if block 163 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 163 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 31 132 minecraft:air if block 147 32 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 147 31 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 31 126 minecraft:air if block 158 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 158 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 31 128 minecraft:air if block 151 32 128 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 151 31 128 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 31 118 minecraft:air if block 155 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 155 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 31 132 minecraft:air if block 155 32 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 155 31 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 31 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 31 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b85 ftm_brick 2
