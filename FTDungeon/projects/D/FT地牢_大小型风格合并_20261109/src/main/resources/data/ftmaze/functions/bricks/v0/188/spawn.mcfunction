scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 834 211 115 minecraft:air if block 834 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 834 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 211 129 minecraft:air if block 850 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 850 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 211 109 minecraft:air if block 849 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 849 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 211 129 minecraft:air if block 830 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 211 124 minecraft:air if block 840 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 840 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 211 118 minecraft:air if block 846 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 846 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 211 109 minecraft:air if block 830 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 211 110 minecraft:air if block 839 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 839 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b188 ftm_brick 2
