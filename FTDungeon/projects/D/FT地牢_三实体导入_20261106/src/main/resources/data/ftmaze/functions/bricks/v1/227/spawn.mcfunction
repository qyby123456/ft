scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1603 151 162 minecraft:air if block 1603 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1603 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 151 146 minecraft:air if block 1618 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 146 minecraft:air if block 1598 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 151 164 minecraft:air if block 1618 152 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 151 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 151 151 minecraft:air if block 1608 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1608 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 151 159 minecraft:air if block 1611 152 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1611 151 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 156 minecraft:air if block 1598 152 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 151 154 minecraft:air if block 1616 152 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1616 151 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1603 151 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1603 151 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b227 ftm_brick 2
