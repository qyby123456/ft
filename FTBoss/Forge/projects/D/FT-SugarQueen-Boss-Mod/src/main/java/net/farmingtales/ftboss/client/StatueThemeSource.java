package net.farmingtales.ftboss.client;

/** 石像界面的显式主题来源；2026-10-24 用于父子页面继承，避免全局主题残留。 */
public interface StatueThemeSource {
    StatueUiTheme statueTheme();
}
