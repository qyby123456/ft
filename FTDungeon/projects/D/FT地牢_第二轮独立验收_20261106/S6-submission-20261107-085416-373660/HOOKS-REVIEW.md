# S6 自动入口、重载与持久锚点独立复跑

结论：上一轮自动load/tick调度与重启接入缺口关闭。整组自然通行与全部功能容器射线交互仍待补证，accepted=false。

冻结754份文件，delivery-manifest SHA256为2221bf270596ec879ca6e6c661283a3d3297f1f68c2b513787a2e6507955ac9b。十源及合并NBT与上一已审版本哈希相同；合并SHA256为5b72d94c89871ebe9c262f92d6c00275ee0c8cfbf32343be96c9cd8e538f1336。接入ZIP仍为fb3334aadeb07db3c48b643e3b9be63ad4f9598c864a1f9ddb53f7b1b97fb0bf。

ZIP全部30文件清单与压缩内容、目录内容逐字节一致；全部data资源（包含minecraft:load/tick标签）与测试资源逐字节相同。此前仅检查fts6_access命名空间的缺口已补齐。

父线程将冻结测试源码、资源和候选复制到independent-hooks-world-offline独立目录，所有运行输出定向至该目录，仅将assembly/summary.json中候选路径指向相同字节的隔离副本。首次新建世界运行，正常保存退出后，第二个JVM使用同一world-parent-isolated世界重启，重启分支不安装、不重新召唤锚点、不手动强加载区块。两个过程均实际运行Forge1.20.1-47.4.10 GameTestServer。

- initial：PID54596，150个延迟等待自然刻，15项断言全部通过。
- restart：PID63628，300个延迟等待自然刻，15项断言全部通过。
- 两阶段均没有显式调用load或tick函数；load自然创建两个trigger目标，tick函数实际位于minecraft:tick标签。
- 权限0模拟玩家自然tick授权进入、撤权后返回、未授权请求拒绝和半径外拒绝通过。
- 两阶段均经MinecraftServer.reloadResources实际重载资源，等待future完成，再次依靠自然tick完成进入与返回。
- 重启等待200刻后，原有origin及lobby锚点各恰好一个，无新增安装或召唤。
- 两轮BUILD SUCCESSFUL，运行后754份冻结证据哈希重新验证未变。

第一份独立复跑在ForgeGradle EnvironmentChecks.testServerConnection的SSL联网预检等待，尚未进入测试。父线程记录jcmd线程诊断并核验自有进程树后取消该次；文件保留在independent-hooks-world。后一次使用Gradle --offline及-Dnet.minecraftforge.gradle.check.certs=false，从缓存构建，跳过该外部连通预检，不修改测试源码、数据包、断言或Java游戏逻辑。该构建参数限制已如实记录，不宣称第一份运行通过。

范围：模拟注册FakePlayer、屏蔽网络包发送；证据支持服务端数据包钩子、权限选择器、传送状态与持久锚点恢复，不是网络客户端确认。仍按原单实例、未旋转overworld、预先预留空场地安装契约使用。不扩大为任意旋转、多实例或所有模组传送防护。

静态载荷、20片、47553屏障、19峡谷纵深与15地面已审结论沿用相同候选哈希；无需重做。后续仅处理必要自然通行及全部功能箱桶/拉杆可达眼位、首命中距离和实际use/复位证据。旧指定位置use与直接槽位操作不冒充该项完成。

独立机器证据：independent-hooks-world-offline/independent-result.json、initial.log、restart.log、assembly/access/hooks-initial-result.json和hooks-restart-result.json。父线程运行器：验收根目录rerun_s6_hooks.py。
