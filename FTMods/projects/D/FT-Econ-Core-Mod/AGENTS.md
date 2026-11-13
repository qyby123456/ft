# AGENTS.md — FT-Econ-Core-Mod（价格 mod, Forge 1.20.1）
modId=`ftecon`，jar=`qys-progressive-taxation-1.0.0.jar`。财富分档定价 + SDM 调价类 + /ftprice 命令；KubeJS 用 `Java.loadClass("net.farmingtales.ftecon.FteconApi")` 检测。与 ftboss（Boss mod）互相独立、勿混改。
构建/部署同 Boss 工程（见 FT-SugarQueen-Boss-Mod/AGENTS.md 的环境与三处部署矩阵）。
部署规则（用户 2026-09-08 确认）：每次更新构建出 jar 后立即同步到三处（主实例 mods、OneDrive桌面成品、Desktop 成品），系统桌面=OneDrive\桌面。
注意：不要在本工程重建/引用 boss 内容。
