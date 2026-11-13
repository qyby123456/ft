scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 884 151 62 minecraft:air if block 884 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 884 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 901 151 54 minecraft:air if block 901 152 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 901 151 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 896 151 72 minecraft:air if block 896 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 896 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 888 151 52 minecraft:air if block 888 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 888 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 893 151 59 minecraft:air if block 893 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 893 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 151 70 minecraft:air if block 881 152 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 881 151 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 890 151 67 minecraft:air if block 890 152 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 890 151 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 899 151 64 minecraft:air if block 899 152 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 899 151 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 885 151 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 885 151 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b193 ftm_brick 2
