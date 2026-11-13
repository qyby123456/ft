scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 91 122 minecraft:air if block 94 92 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 94 91 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 91 132 minecraft:air if block 105 92 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 105 91 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 91 118 minecraft:air if block 109 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 109 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 91 132 minecraft:air if block 90 92 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 90 91 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 91 116 minecraft:air if block 99 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 99 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 91 129 minecraft:air if block 97 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 97 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 91 124 minecraft:air if block 102 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 102 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 91 126 minecraft:air if block 108 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 108 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 91 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 91 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b53 ftm_brick 2
