scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 91 149 minecraft:air if block 178 92 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 178 91 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 91 160 minecraft:air if block 193 92 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 193 91 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 91 147 minecraft:air if block 191 92 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 191 91 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 91 160 minecraft:air if block 175 92 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 175 91 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 91 154 minecraft:air if block 186 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 186 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 91 156 minecraft:air if block 179 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 179 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 91 146 minecraft:air if block 183 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 183 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 91 160 minecraft:air if block 183 92 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 183 91 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 91 150 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 91 150 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b66 ftm_brick 2
