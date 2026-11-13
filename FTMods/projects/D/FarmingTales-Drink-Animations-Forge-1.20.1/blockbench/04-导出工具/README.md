# 导出工具

从项目根目录运行脚本；素材与输出目录已按新的分类布局调整。

| 任务 | 脚本 |
| --- | --- |
| 普通酒水 | export_drinks_v54.py |
| 果酱基准 | export_jam_v54.py |
| 全部酱罐 | export_jar_family_v54.py |
| 巧克力酱 | export_chocolate_v55.py |
| 鸡尾酒 | export_cocktails_v56.py |
| 纸鸢完整工程 | export_papercraft_v68.py |
| 光 S 补充 | export_guang_v69.py |
| Vinery / Brewery | export_letsdo_v70.py |

导出前先用项目 Gradle 任务生成 build/blockbench 中的采样数据。
export_elbow_v14.py 提供公共拟合函数和手臂模板导出；camera-source 保留相机插件。
旧版脚本目录只作算法参考。其历史工程和测试图已删除，不作为当前生成入口。
