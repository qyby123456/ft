forceload add 1008 1024 1263 1151
fill 1024 128 1024 1055 152 1055 minecraft:air replace
fill 1024 128 1056 1055 152 1087 minecraft:air replace
fill 1024 128 1088 1055 152 1119 minecraft:air replace
fill 1024 128 1120 1055 152 1149 minecraft:air replace
fill 1056 128 1024 1087 152 1055 minecraft:air replace
fill 1056 128 1056 1087 152 1087 minecraft:air replace
fill 1056 128 1088 1087 152 1119 minecraft:air replace
fill 1056 128 1120 1087 152 1149 minecraft:air replace
fill 1088 128 1024 1119 152 1055 minecraft:air replace
fill 1088 128 1056 1119 152 1087 minecraft:air replace
fill 1088 128 1088 1119 152 1119 minecraft:air replace
fill 1088 128 1120 1119 152 1149 minecraft:air replace
fill 1120 128 1024 1151 152 1055 minecraft:air replace
fill 1120 128 1056 1151 152 1087 minecraft:air replace
fill 1120 128 1088 1151 152 1119 minecraft:air replace
fill 1120 128 1120 1151 152 1149 minecraft:air replace
fill 1152 128 1024 1183 152 1055 minecraft:air replace
fill 1152 128 1056 1183 152 1087 minecraft:air replace
fill 1152 128 1088 1183 152 1119 minecraft:air replace
fill 1152 128 1120 1183 152 1149 minecraft:air replace
fill 1184 128 1024 1215 152 1055 minecraft:air replace
fill 1184 128 1056 1215 152 1087 minecraft:air replace
fill 1184 128 1088 1215 152 1119 minecraft:air replace
fill 1184 128 1120 1215 152 1149 minecraft:air replace
fill 1216 128 1024 1247 152 1055 minecraft:air replace
fill 1216 128 1056 1247 152 1087 minecraft:air replace
fill 1216 128 1088 1247 152 1119 minecraft:air replace
fill 1216 128 1120 1247 152 1149 minecraft:air replace
fill 1248 128 1024 1262 152 1055 minecraft:air replace
fill 1248 128 1056 1262 152 1087 minecraft:air replace
fill 1248 128 1088 1262 152 1119 minecraft:air replace
fill 1248 128 1120 1262 152 1149 minecraft:air replace
fill 1014 127 1041 1022 133 1049 minecraft:air replace
fill 1014 127 1041 1022 127 1049 minecraft:cut_sandstone
setblock 1018 127 1045 minecraft:chiseled_sandstone
execute store success score #placed s2_state run place template s2access:desert_33ceb33f 1024 128 1024 none none 1.0 0
execute if score #placed s2_state matches 1 run scoreboard players set #installed s2_state 1
forceload remove 1008 1024 1263 1151
