scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1603 91 76 minecraft:air if block 1603 92 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1603 91 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 91 88 minecraft:air if block 1618 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1618 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1617 91 68 minecraft:air if block 1617 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1617 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 91 88 minecraft:air if block 1598 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 91 68 minecraft:air if block 1598 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 91 81 minecraft:air if block 1611 92 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1611 91 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1605 91 85 minecraft:air if block 1605 92 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1605 91 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 91 69 minecraft:air if block 1606 92 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1606 91 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 91 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 91 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b232 ftm_brick 2
