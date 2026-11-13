scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 183 91 154 minecraft:air if block 183 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 183 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 91 145 minecraft:air if block 194 92 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 194 91 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 145 minecraft:air if block 174 92 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 91 158 minecraft:air if block 193 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 193 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 91 158 minecraft:air if block 175 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 175 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 91 144 minecraft:air if block 184 92 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 184 91 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 91 150 minecraft:air if block 189 92 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 189 91 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 91 151 minecraft:air if block 177 92 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 177 91 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 184 91 154 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 184 91 154 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b66 ftm_brick 2
