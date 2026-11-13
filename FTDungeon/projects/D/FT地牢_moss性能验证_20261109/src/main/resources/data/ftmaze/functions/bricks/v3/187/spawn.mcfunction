scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 835 91 162 minecraft:air if block 835 92 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 835 91 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 91 146 minecraft:air if block 850 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 850 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 91 146 minecraft:air if block 830 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 91 164 minecraft:air if block 850 92 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 850 91 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 91 151 minecraft:air if block 840 92 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 840 91 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 91 159 minecraft:air if block 843 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 843 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 91 156 minecraft:air if block 830 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 91 154 minecraft:air if block 848 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 848 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 835 91 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 835 91 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b187 ftm_brick 2
