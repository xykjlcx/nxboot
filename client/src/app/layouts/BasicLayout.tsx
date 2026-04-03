import { useState, useMemo } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { Layout, Menu, Dropdown, Button, Popover, Breadcrumb, Tooltip, Watermark, type MenuProps } from "antd";
import {
  AppstoreOutlined,
  BulbOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  TeamOutlined,
  SettingOutlined,
  ApartmentOutlined,
  BookOutlined,
  EditOutlined,
  FileSearchOutlined,
  FileOutlined,
  ClockCircleOutlined,
  ScheduleOutlined,
  SafetyCertificateOutlined,
  DashboardOutlined,
  DatabaseOutlined,
} from "@ant-design/icons";
import { useAuth, type MenuVO } from "@/shared/hooks/useAuth";
import { useTheme } from "@/shared/hooks/useTheme";
import { usePreferences } from "@/shared/stores/preferences";
import styles from "./BasicLayout.module.css";

const { Header, Sider, Content } = Layout;

/** 显式图标注册表——只包含实际使用的图标，支持 tree-shaking */
const iconRegistry: Record<string, React.ComponentType> = {
  // 导航/布局图标
  AppstoreOutlined, MenuFoldOutlined, MenuUnfoldOutlined,
  // 用户操作图标
  UserOutlined, LogoutOutlined, BulbOutlined,
  // 菜单图标（与 sys_menu.icon 字段对应）
  SettingOutlined, TeamOutlined, ApartmentOutlined, BookOutlined,
  EditOutlined, FileSearchOutlined, FileOutlined, FileTextOutlined,
  ClockCircleOutlined, ScheduleOutlined, SafetyCertificateOutlined,
  DashboardOutlined, DatabaseOutlined,
  // 别名映射（sys_menu 中的短名 → 实际组件）
  Setting: SettingOutlined,
  User: UserOutlined,
  Peoples: TeamOutlined,
  TreeTable: ApartmentOutlined,
  Dict: BookOutlined,
  Edit: EditOutlined,
  Log: FileSearchOutlined,
  Document: FileOutlined,
  Job: ClockCircleOutlined,
  Solution: BulbOutlined,
};

/** 根据图标名称动态获取 Ant Design 图标组件 */
function getIcon(name: string | null | undefined): React.ReactNode {
  if (!name) return <FileTextOutlined />;
  // 精确匹配 → 追加 Outlined 后缀匹配 → 兜底
  const IconComp = iconRegistry[name] ?? iconRegistry[name + "Outlined"];
  return IconComp ? <IconComp /> : <FileTextOutlined />;
}

/* ──────────── 子系统 → 模块 → 菜单 三级配置 ──────────── */

interface SideMenuItem {
  key: string;
  icon: React.ReactNode;
  label: string;
}

interface ModuleConfig {
  key: string;
  label: string;
  menus: SideMenuItem[];
}

interface SubsystemConfig {
  key: string;
  label: string;
  icon: React.ReactNode;
  modules: ModuleConfig[];
}

/**
 * 将后端菜单树转换为前端三级导航配置
 *
 * 后端菜单树结构（三级）：
 *   子系统(M, parentId=0) → 模块(M) → 页面(C)
 *
 * 如果子系统下没有 M 类型子节点（扁平结构），
 * 则将其 C 类型子节点包装为单个默认模块
 */
function menusToSubsystems(menus: MenuVO[]): SubsystemConfig[] {
  return menus
    .filter((m) => m.menuType === "M")
    .map((sub) => {
      const children = sub.children ?? [];
      // 子级中的目录节点 → 模块
      const modules: ModuleConfig[] = children
        .filter((c) => c.menuType === "M")
        .map((mod) => ({
          key: mod.path || mod.id.toString(),
          label: mod.menuName,
          menus:
            mod.children
              ?.filter((item) => item.menuType === "C")
              .map((item) => ({
                key: item.path || "",
                icon: getIcon(item.icon),
                label: item.menuName,
              })) ?? [],
        }));

      // 扁平结构：子系统直接包含菜单项，无中间模块层
      if (modules.length === 0) {
        const directMenus = children
          .filter((c) => c.menuType === "C")
          .map((item) => ({
            key: item.path || "",
            icon: getIcon(item.icon),
            label: item.menuName,
          }));
        return {
          key: sub.path || sub.id.toString(),
          label: sub.menuName,
          icon: getIcon(sub.icon),
          modules: [{ key: `${sub.path || sub.id}-default`, label: sub.menuName, menus: directMenus }],
        };
      }

      return {
        key: sub.path || sub.id.toString(),
        label: sub.menuName,
        icon: getIcon(sub.icon),
        modules,
      };
    });
}

/* ──────────── 组件 ──────────── */

export function BasicLayout() {
  const [popoverOpen, setPopoverOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const user = useAuth((s) => s.user);
  const backendMenus = useAuth((s) => s.menus);
  const logout = useAuth((s) => s.logout);
  const { isDark, toggle: toggleTheme } = useTheme();
  const prefs = usePreferences();

  const toggleCollapsed = () => prefs.update({ sidebarCollapsed: !prefs.sidebarCollapsed });

  // 将后端菜单树转换为前端导航配置
  const subsystems = useMemo(() => menusToSubsystems(backendMenus), [backendMenus]);

  // 当前子系统
  const activeSubsystem = useMemo(() => {
    if (subsystems.length === 0) return null;
    const seg = location.pathname.split("/")[1];
    return subsystems.find((s) => s.key === seg) ?? subsystems[0]!;
  }, [location.pathname, subsystems]) as SubsystemConfig;

  // 当前模块
  const activeModule = useMemo(() => {
    const path = location.pathname;
    return (
      activeSubsystem.modules.find((m) => m.menus.some((menu) => path.startsWith(menu.key))) ??
      activeSubsystem.modules[0]!
    );
  }, [location.pathname, activeSubsystem]) as ModuleConfig;

  const handleSubsystemClick = (sub: SubsystemConfig) => {
    const firstMenu = sub.modules[0]?.menus[0];
    if (firstMenu) navigate(firstMenu.key);
    setPopoverOpen(false);
  };

  const handleModuleClick = (mod: ModuleConfig) => {
    const firstMenu = mod.menus[0];
    if (firstMenu) navigate(firstMenu.key);
  };

  const handleMenuClick: MenuProps["onClick"] = ({ key }) => navigate(key);

  // 面包屑
  const breadcrumbItems = useMemo(() => {
    if (!prefs.showBreadcrumb) return [];
    const currentMenu = activeModule.menus.find((m) => location.pathname.startsWith(m.key));
    return [
      { title: activeSubsystem.label },
      ...(activeSubsystem.modules.length > 1 ? [{ title: activeModule.label }] : []),
      ...(currentMenu ? [{ title: currentMenu.label }] : []),
    ];
  }, [location.pathname, activeSubsystem, activeModule, prefs.showBreadcrumb]);

  // 用户下拉菜单
  const userMenuItems: MenuProps["items"] = [
    { key: "theme", icon: <BulbOutlined />, label: isDark ? "浅色模式" : "深色模式", onClick: toggleTheme },
    { type: "divider" },
    { key: "logout", icon: <LogoutOutlined />, label: "退出登录", onClick: logout },
  ];

  const headerStyle = {
    height: prefs.headerHeight,
    lineHeight: `${prefs.headerHeight}px`,
    padding: "0 16px",
  };

  // 九宫格弹框
  const subsystemGrid = (
    <div className={styles.subsystemGrid}>
      {subsystems.map((s) => (
        <div
          key={s.key}
          className={`${styles.subsystemCard} ${activeSubsystem.key === s.key ? styles.subsystemCardActive : ""}`}
          onClick={() => handleSubsystemClick(s)}
        >
          <span className={styles.subsystemIcon}>{s.icon}</span>
          <span className={styles.subsystemLabel}>{s.label}</span>
        </div>
      ))}
    </div>
  );

  // 公共 Header 右侧
  const headerRight = (
    <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
      <Button type="text" icon={<UserOutlined />} className={styles.userBtn}>
        {user?.username ?? "用户"}
      </Button>
    </Dropdown>
  );

  // 公共九宫格按钮
  const gridButton = (
    <Popover content={subsystemGrid} trigger="click" open={popoverOpen} onOpenChange={setPopoverOpen} placement="bottomLeft" arrow={false}>
      <div className={styles.gridBtn}><AppstoreOutlined /></div>
    </Popover>
  );

  // 公共面包屑
  const breadcrumb = prefs.showBreadcrumb && breadcrumbItems.length > 0 ? (
    <Breadcrumb items={breadcrumbItems} className={styles.breadcrumb} />
  ) : null;

  // 公共内容区（含水印）
  const contentBody = (
    <Watermark content={user?.username} font={{ fontSize: 14, color: "rgba(0,0,0,0.03)" }}>
      {breadcrumb}
      <Outlet />
    </Watermark>
  );

  // 公共折叠按钮
  const collapseButton = (
    <div className={styles.siderFooter}>
      <Tooltip title={prefs.sidebarCollapsed ? "展开菜单" : "收起菜单"} placement="right">
        <Button type="text" icon={prefs.sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />} onClick={toggleCollapsed} className={styles.collapseBtn} />
      </Tooltip>
    </div>
  );

  /* ── top 布局：水平菜单在 header ── */
  if (prefs.layoutMode === "top") {
    const topMenuItems = activeSubsystem.modules.flatMap((m) => m.menus) as MenuProps["items"];
    return (
      <Layout className={styles.layout}>
        <Header className={styles.header} style={headerStyle}>
          <div className={styles.headerLeft}>
            {gridButton}
            <div className={styles.logo}>NxBoot</div>
            <Menu mode="horizontal" selectedKeys={[location.pathname]} items={topMenuItems} onClick={handleMenuClick} className={styles.topMenu} />
          </div>
          {headerRight}
        </Header>
        <Content className={styles.contentFull}>
          {contentBody}
        </Content>
      </Layout>
    );
  }

  /* ── sidebar 布局：经典左右分栏 ── */
  if (prefs.layoutMode === "sidebar") {
    const sidebarItems = (activeSubsystem.modules.length === 1
      ? activeSubsystem.modules[0]!.menus
      : activeSubsystem.modules.map((mod) => ({ key: mod.key, label: mod.label, type: "group" as const, children: mod.menus }))) as MenuProps["items"];

    return (
      <Layout className={styles.layout} style={{ flexDirection: "row" }}>
        <Sider width={prefs.sidebarWidth} collapsedWidth={prefs.sidebarCollapsedWidth} collapsed={prefs.sidebarCollapsed} style={{ background: "var(--color-bg-card, #fff)" }} className={styles.siderFull}>
          <div className={styles.siderBrand} style={{ height: prefs.headerHeight }}>
            {!prefs.sidebarCollapsed && <span className={styles.siderBrandText}>NxBoot</span>}
          </div>
          <Menu mode="inline" inlineCollapsed={prefs.sidebarCollapsed} selectedKeys={[location.pathname]} items={sidebarItems} onClick={handleMenuClick} className={styles.siderMenu} />
          {collapseButton}
        </Sider>
        <Layout>
          <Header className={styles.header} style={headerStyle}>
            <div className={styles.headerLeft}>{gridButton}</div>
            {headerRight}
          </Header>
          <Content className={styles.content} style={{ borderRadius: prefs.contentBorderRadius }}>
            {breadcrumb}
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    );
  }

  /* ── mix 布局（默认）：顶部模块 tab + 侧边子菜单 ── */
  return (
    <Layout className={styles.layout}>
      <Header className={styles.header} style={headerStyle}>
        <div className={styles.headerLeft}>
          {gridButton}
          <div className={styles.logo}>NxBoot</div>
          <div className={styles.headerTabs}>
            {activeSubsystem.modules.map((m) => (
              <div key={m.key} className={`${styles.headerTab} ${activeModule.key === m.key ? styles.headerTabActive : ""}`} onClick={() => handleModuleClick(m)}>
                {m.label}
              </div>
            ))}
          </div>
        </div>
        {headerRight}
      </Header>
      <Layout className={styles.body}>
        <Sider width={prefs.sidebarWidth} collapsedWidth={prefs.sidebarCollapsedWidth} collapsed={prefs.sidebarCollapsed} style={{ background: "var(--color-bg-card, #fff)" }} className={styles.sider}>
          <Menu mode="inline" inlineCollapsed={prefs.sidebarCollapsed} selectedKeys={[location.pathname]} items={activeModule.menus as MenuProps["items"]} onClick={handleMenuClick} className={styles.siderMenu} />
          {collapseButton}
        </Sider>
        <Content className={styles.content} style={{ borderRadius: prefs.contentBorderRadius }}>
          {contentBody}
        </Content>
      </Layout>
    </Layout>
  );
}
