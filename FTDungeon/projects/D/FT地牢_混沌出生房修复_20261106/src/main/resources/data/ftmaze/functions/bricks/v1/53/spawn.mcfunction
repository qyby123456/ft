scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 105 91 121 minecraft:air if block 105 92 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 105 91 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 91 128 minecraft:air if block 91 92 128 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 91 91 128 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 91 118 minecraft:air if block 94 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 94 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 91 130 minecraft:air if block 109 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 109 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 91 124 minecraft:air if block 98 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 98 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 101 91 116 minecraft:air if block 101 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 101 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 91 130 minecraft:air if block 100 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 100 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 91 126 minecraft:air if block 103 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 103 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 106 91 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 106 91 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b53 ftm_brick 2
