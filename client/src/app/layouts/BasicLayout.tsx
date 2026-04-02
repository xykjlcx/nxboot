import { useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { Layout, Menu, Dropdown, Button, type MenuProps } from "antd";
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  AppstoreOutlined,
  SettingOutlined,
  FileTextOutlined,
  CloudUploadOutlined,
  ClockCircleOutlined,
  BookOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import { useAuth } from "@/shared/hooks/useAuth";
import styles from "./BasicLayout.module.css";

const { Header, Sider, Content } = Layout;

/** 侧边菜单项（暂时硬编码，后续可改为动态加载） */
const menuItems: MenuProps["items"] = [
  {
    key: "/system",
    icon: <SettingOutlined />,
    label: "系统管理",
    children: [
      { key: "/system/user", icon: <UserOutlined />, label: "用户管理" },
      { key: "/system/role", icon: <SafetyCertificateOutlined />, label: "角色管理" },
      { key: "/system/menu", icon: <AppstoreOutlined />, label: "菜单管理" },
      { key: "/system/dict", icon: <BookOutlined />, label: "字典管理" },
      { key: "/system/config", icon: <SettingOutlined />, label: "系统配置" },
      { key: "/system/log", icon: <FileTextOutlined />, label: "操作日志" },
      { key: "/system/file", icon: <CloudUploadOutlined />, label: "文件管理" },
      { key: "/system/job", icon: <ClockCircleOutlined />, label: "定时任务" },
    ],
  },
];

export function BasicLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const user = useAuth((s) => s.user);
  const logout = useAuth((s) => s.logout);

  const handleMenuClick: MenuProps["onClick"] = ({ key }) => {
    navigate(key);
  };

  const userMenuItems: MenuProps["items"] = [
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: "退出登录",
      onClick: logout,
    },
  ];

  // 找到当前路径对应的打开的 submenu key
  const openKeys = [`/${location.pathname.split("/")[1]}`];

  return (
    <Layout className={styles.layout}>
      <Sider
        className={styles.sider}
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={220}
      >
        <div className={styles.logo}>{collapsed ? "Nx" : "NxBoot"}</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={openKeys}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 220, transition: "margin-left 0.2s" }}>
        <Header className={styles.header}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
          />
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Button type="text" icon={<TeamOutlined />}>
              {user?.username ?? "用户"}
            </Button>
          </Dropdown>
        </Header>
        <Content className={styles.content}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
