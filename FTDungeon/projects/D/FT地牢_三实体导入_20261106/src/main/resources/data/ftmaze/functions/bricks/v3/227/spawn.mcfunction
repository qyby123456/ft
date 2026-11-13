scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1603 91 162 minecraft:air if block 1603 92 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1603 91 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 91 146 minecraft:air if block 1618 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1618 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 91 146 minecraft:air if block 1598 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 91 164 minecraft:air if block 1618 92 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1618 91 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 91 151 minecraft:air if block 1608 92 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1608 91 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 91 159 minecraft:air if block 1611 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1611 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 91 156 minecraft:air if block 1598 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 91 154 minecraft:air if block 1616 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1616 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1603 91 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1603 91 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b227 ftm_brick 2
