execute in minecraft:overworld run forceload add 2028 1024 2264 1117
execute in minecraft:overworld run place template s5access:part_01 2048 120 1024
execute in minecraft:overworld run place template s5access:part_02 2048 120 1072
execute in minecraft:overworld run place template s5access:part_03 2096 120 1024
execute in minecraft:overworld run place template s5access:part_04 2096 120 1072
execute in minecraft:overworld run place template s5access:part_05 2144 120 1024
execute in minecraft:overworld run place template s5access:part_06 2144 120 1072
execute in minecraft:overworld run place template s5access:part_07 2192 120 1024
execute in minecraft:overworld run place template s5access:part_08 2192 120 1072
execute in minecraft:overworld run place template s5access:part_09 2240 120 1024
execute in minecraft:overworld run place template s5access:part_10 2240 120 1072
execute in minecraft:overworld run place template s5access:lobby 2028 120 1043
execute in minecraft:overworld run forceload remove 2028 1024 2264 1117
function s5access:load
tellraw @a {"text":"S5 access installed: lobby 2032.5 121 1047.5; /trigger s5a_enter; finish /trigger s5a_exit","color":"aqua"}
