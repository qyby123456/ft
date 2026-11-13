scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 121 91 148 minecraft:air if block 121 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 121 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 91 158 minecraft:air if block 137 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 137 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 91 147 minecraft:air if block 134 92 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 134 91 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 124 91 158 minecraft:air if block 124 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 124 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 91 152 minecraft:air if block 128 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 128 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 91 155 minecraft:air if block 119 92 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 91 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 91 146 minecraft:air if block 127 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 127 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 91 157 minecraft:air if block 130 92 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 130 91 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 91 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 91 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b58 ftm_brick 2
