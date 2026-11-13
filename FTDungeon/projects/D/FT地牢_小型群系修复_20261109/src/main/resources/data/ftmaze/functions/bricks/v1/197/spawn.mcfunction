scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1027 211 162 minecraft:air if block 1027 212 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1027 211 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 211 146 minecraft:air if block 1042 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1042 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 211 146 minecraft:air if block 1022 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 211 164 minecraft:air if block 1042 212 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1042 211 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 211 151 minecraft:air if block 1032 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1032 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1035 211 159 minecraft:air if block 1035 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1035 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 211 156 minecraft:air if block 1022 212 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 211 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 211 154 minecraft:air if block 1040 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1040 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1027 211 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1027 211 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b197 ftm_brick 2
