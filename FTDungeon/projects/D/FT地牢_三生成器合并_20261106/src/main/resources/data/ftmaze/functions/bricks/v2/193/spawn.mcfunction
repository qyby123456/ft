scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 884 151 62 minecraft:air if block 884 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 884 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 151 77 minecraft:air if block 900 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 900 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 151 57 minecraft:air if block 900 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 900 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 151 77 minecraft:air if block 880 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 880 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 890 151 71 minecraft:air if block 890 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 890 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 151 66 minecraft:air if block 897 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 897 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 151 57 minecraft:air if block 889 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 889 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 151 57 minecraft:air if block 880 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 880 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 884 151 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 884 151 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b193 ftm_brick 2
