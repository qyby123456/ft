scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 835 211 162 minecraft:air if block 835 212 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 835 211 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 211 146 minecraft:air if block 850 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 850 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 211 146 minecraft:air if block 830 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 211 164 minecraft:air if block 850 212 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 850 211 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 211 151 minecraft:air if block 840 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 840 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 211 159 minecraft:air if block 843 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 843 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 211 156 minecraft:air if block 830 212 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 211 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 211 154 minecraft:air if block 848 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 848 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 835 211 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 835 211 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b187 ftm_brick 2
