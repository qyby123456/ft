scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 133 91 149 minecraft:air if block 133 92 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 133 91 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 91 156 minecraft:air if block 119 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 91 146 minecraft:air if block 122 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 122 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 91 158 minecraft:air if block 137 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 137 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 126 91 152 minecraft:air if block 126 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 126 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 129 91 144 minecraft:air if block 129 92 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 129 91 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 91 158 minecraft:air if block 128 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 128 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 91 154 minecraft:air if block 131 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 131 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 134 91 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 134 91 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b58 ftm_brick 2
