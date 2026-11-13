scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 884 211 62 minecraft:air if block 884 212 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 884 211 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 211 77 minecraft:air if block 900 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 900 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 211 57 minecraft:air if block 900 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 900 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 211 77 minecraft:air if block 880 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 880 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 890 211 71 minecraft:air if block 890 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 890 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 211 66 minecraft:air if block 897 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 897 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 211 57 minecraft:air if block 889 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 889 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 211 57 minecraft:air if block 880 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 880 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 884 211 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 884 211 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b193 ftm_brick 2
