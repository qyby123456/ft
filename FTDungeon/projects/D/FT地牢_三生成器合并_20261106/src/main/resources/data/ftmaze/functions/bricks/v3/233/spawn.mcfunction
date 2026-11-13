scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1652 91 62 minecraft:air if block 1652 92 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1652 91 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 91 77 minecraft:air if block 1668 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1668 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 91 57 minecraft:air if block 1668 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1668 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 91 77 minecraft:air if block 1648 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1648 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1658 91 71 minecraft:air if block 1658 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1658 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 91 66 minecraft:air if block 1665 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1665 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1657 91 57 minecraft:air if block 1657 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1657 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 91 57 minecraft:air if block 1648 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1648 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1652 91 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1652 91 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b233 ftm_brick 2
