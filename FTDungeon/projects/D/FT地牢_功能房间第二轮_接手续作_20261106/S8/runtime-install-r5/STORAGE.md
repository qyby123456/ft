# 接入补证世界与缓存已外置

本说明是当前存储位置入口；原README、INSTALLATION-EVIDENCE.json、输入和日志保留执行时原文与哈希，不回写冻结历史。请勿运行旧D盘run.ps1或prepare.mjs重建世界。

当前完成安装的实际世界：

`C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s8-maker-access-isolated\transfer-bd97e24b51ec\runtime-install-r5\server\world`

同一transfer目录完整保存本轮runtime-install-r1至r5的server目录，以及本轮可重建build/.gradle缓存。没有迁移旧冻结运行测试、其他组或正式实例。共392个文件、115602299字节，迁前迁后SHA256与长度逐个一致；没有删除世界或重启测试。

STORAGE-MANIFEST.json给出每个文件的original_path、当前path、sha256、bytes，以及原INSTALLATION-EVIDENCE.json哈希。读取旧证据里的路径时，若命中某一迁移source目录，则以对应destination加相同相对路径解析。旧证据的103项资产哈希仍有效；实际世界、数据包和服务器日志现位于C盘。D盘保留源码、报告、输入、清单及两份小型交付ZIP。

后续只允许在指定C盘根下新建复现目录。使用本目录新增的prepare-external.mjs，例如在任意目录执行：

```powershell
node 'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S8\runtime-install-r5\prepare-external.mjs' fresh-access-r6
```

该脚本拒绝覆盖已有目录，只复制源码、已验证的数据包与输入，不复制已有世界区域文件；世界和Gradle编译输出均落在新C盘子目录。之后如需重新执行，只运行新C目录中的run.ps1。此次存储补充没有启动新测试，也未再次安装候选。

本次安装的功能结果、冻结候选4d768bc...与20分片均保持不变；迁移是保留全部文件的存储调整，不新增放行声明。
