scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1027 31 162 minecraft:air if block 1027 32 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1027 31 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 31 146 minecraft:air if block 1042 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1042 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 31 146 minecraft:air if block 1022 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 31 164 minecraft:air if block 1042 32 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1042 31 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 31 151 minecraft:air if block 1032 32 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1032 31 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1035 31 159 minecraft:air if block 1035 32 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1035 31 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 31 156 minecraft:air if block 1022 32 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 31 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 31 154 minecraft:air if block 1040 32 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1040 31 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1027 31 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1027 31 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b197 ftm_brick 2
