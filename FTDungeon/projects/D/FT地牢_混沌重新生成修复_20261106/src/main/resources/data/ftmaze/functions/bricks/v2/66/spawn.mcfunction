scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 91 151 minecraft:air if block 178 92 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 178 91 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 91 158 minecraft:air if block 194 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 194 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 91 146 minecraft:air if block 189 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 189 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 91 159 minecraft:air if block 182 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 182 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 157 minecraft:air if block 174 92 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 91 153 minecraft:air if block 186 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 186 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 146 minecraft:air if block 174 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 91 147 minecraft:air if block 182 92 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 182 91 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 91 150 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 91 150 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b66 ftm_brick 2
