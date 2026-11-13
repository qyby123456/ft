scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 126 91 102 minecraft:air if block 126 92 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 126 91 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 91 99 minecraft:air if block 137 92 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 137 91 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 91 110 minecraft:air if block 122 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 122 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 91 108 minecraft:air if block 131 92 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 131 91 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 91 99 minecraft:air if block 119 92 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 91 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 91 98 minecraft:air if block 130 92 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 130 91 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 91 107 minecraft:air if block 125 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 125 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 91 101 minecraft:air if block 133 92 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 133 91 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 127 91 102 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 127 91 102 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b56 ftm_brick 2
