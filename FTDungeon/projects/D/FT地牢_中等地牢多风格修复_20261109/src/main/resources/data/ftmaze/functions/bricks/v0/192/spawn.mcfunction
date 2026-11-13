scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 835 211 76 minecraft:air if block 835 212 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 835 211 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 211 88 minecraft:air if block 850 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 850 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 211 68 minecraft:air if block 849 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 849 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 211 88 minecraft:air if block 830 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 211 68 minecraft:air if block 830 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 211 81 minecraft:air if block 843 212 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 843 211 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 211 85 minecraft:air if block 837 212 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 837 211 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 211 69 minecraft:air if block 838 212 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 838 211 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 211 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 211 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b192 ftm_brick 2
