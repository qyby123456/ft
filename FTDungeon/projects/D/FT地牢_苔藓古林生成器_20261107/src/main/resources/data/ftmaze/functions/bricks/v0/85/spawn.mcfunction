scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 31 123 minecraft:air if block 150 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 150 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 31 130 minecraft:air if block 166 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 166 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 31 118 minecraft:air if block 161 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 161 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 31 131 minecraft:air if block 154 32 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 154 31 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 31 129 minecraft:air if block 146 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 146 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 31 125 minecraft:air if block 158 32 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 158 31 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 31 118 minecraft:air if block 146 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 146 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 31 119 minecraft:air if block 154 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 154 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 31 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 31 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b85 ftm_brick 2
