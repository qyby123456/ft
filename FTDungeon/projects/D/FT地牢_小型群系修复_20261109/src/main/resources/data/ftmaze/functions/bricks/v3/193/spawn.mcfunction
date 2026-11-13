scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 884 91 62 minecraft:air if block 884 92 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 884 91 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 91 77 minecraft:air if block 900 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 900 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 91 57 minecraft:air if block 900 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 900 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 91 77 minecraft:air if block 880 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 880 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 890 91 71 minecraft:air if block 890 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 890 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 91 66 minecraft:air if block 897 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 897 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 91 57 minecraft:air if block 889 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 889 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 91 57 minecraft:air if block 880 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 880 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 884 91 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 884 91 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b193 ftm_brick 2
