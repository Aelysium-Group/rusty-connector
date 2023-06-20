![Aelysium Wordmark Image](https://github.com/Aelysium-Group/.github/blob/main/images/rustyconnector-wordmark.png?raw=true)

# 👋 欢迎使用RustyConnector
![Velocity](https://badgen.net/badge/Velocity/3.1.1%20-%203.2.0/1197d1?icon=dockbit)
![PaperMC](https://badgen.net/badge/Paper/1.16%20-%201.20.1/F96854?icon=telegram)
![Folia](https://badgen.net/badge/Folia/Supported/E004BC?icon=maven)
[![Aelysium](https://badgen.net/badge/Discord/Aelysium/5865F2?icon=discord)](https://join.aelysium.group/)
[![Build](https://badgen.net/github/release/Aelysium-Group/rusty-connector?label=Latest%20Stable%20Release&icon=maven)](https://github.com/Aelysium-Group/rusty-connector/releases)

RustyConnector是一款可运行于Velocity/Paper群组网络的负载均衡插件，它可以让你的服务器在运行状态下自动向代理端注册或注销子服务器，并实现玩家之间的负载均衡

<br>

<table>
<thead>
<tr>
<th width="2000" colspan="2">
</th>
</tr>
<h2>🧭 快速链接</h2>
</thead>
<tbody>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://github.com/Aelysium-Group/rusty-connector/wiki"><img src="../images/logo/rc-logo.webp"></a>
  </td>
  <td valign="top">
    <h3>Wiki</h3>
    <p>
      不知道如何使用本插件？<a href="https://github.com/Aelysium-Group/rusty-connector/wiki">点击此链接</a>教你快速入门。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://github.com/Aelysium-Group/rusty-connector/releases"><img src="../images/logo/rc-logo.webp"></a>
  </td>
  <td valign="top">
    <h3>下载插件</h3>
    <p>
      <a href="https://github.com/Aelysium-Group/rusty-connector/releases">点击前往Github Releases下载</a>，或前往插件发布页。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://join.aelysium.group"><img src="../images/logo/rc-logo.webp"></a>
  </td>
  <td>
    <h3>帮助支持</h3>
    <p>
      您可<a href="https://join.aelysium.group">点击此链接</a>加入我们的Discord社区获得更多帮助。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://github.com/Aelysium-Group/rusty-connector/issues"><img src="../images/logo/rc-logo.webp"></a>
  </td>
  <td>
    <h3>BUG反馈</h3>
    <p>
      在使用插件的时候遇到了烦人的BUG？ 请<a href="https://github.com/Aelysium-Group/rusty-connector/issues">点击此链接</a>与我们反馈。
    </p>
  </td>
</tr>
</tbody>
</table>

<table>
<thead>
<tr>
<th width="2000" colspan="2">
</th>
</tr>
<h2>🚀 插件发布页</h2>
</thead>
<tbody>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://modrinth.com/plugin/rustyconnector"><img src="../images/logo/modrinth.svg"></a>
  </td>
  <td valign="top">
    <h3>Modrinth</h3>
    <p>
      <a href="https://modrinth.com/plugin/rustyconnector">点击前往Modrinth插件发布页面。</a>
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://hangar.papermc.io/nathan-i-martin/RustyConnector"><img src="../images/logo/hanger.svg"></a>
  </td>
  <td valign="top">
    <h3>Hanger</h3>
    <p>
      <a href="https://hangar.papermc.io/nathan-i-martin/RustyConnector">点击前往Hanger插件发布页。</a>
    </p>
  </td>
</tr>
</tbody>
</table>

<br>

---
# ✨ 特色功能
- ### ✅ 为构建大型群组服而生
- ### ✅ 可在运行期间自动向代理端注册全新的子服务器
- ### ✅ 与Redis集成实现超快速度传输
- ### ✅ 可创建预定义的白名单配置并动态开启或关闭它们
- ### ✅ 可通过家族级白名单和负载平衡将类似的服务器注册成家族
- ### ✅ 可自动从代理端中取消注册被冻结的服务器
- ### ✅ 可为你的服务器添加软/硬人数限制
- ### ✅ 专为有状态的Minecraft服务器所打造!
- ### ✅ 支持基于用户名，权限组，UUID和IP地址等形式的白名单功能
- ### ✅ 允许玩家跨服/tpa传送
- ### ✅ 可与LuckPerms-Velocity一起使用
- ### ✅ 支持Kubernetes容器化部署
- ### ✅ 支持Folia服务端
- ### ❌ 它不能做到给你个拥抱或Rickroll了你

---
# 🎨 数据统计


## 🌌 代理端数据统计:
[![RustyConnector bstats graph](https://bstats.org/signatures/velocity/RustyConnector.svg)](https://bstats.org/signatures/velocity/RustyConnector.svg)
## 🌌 子服务器数据统计:
[![RustyConnector bstats graph](https://bstats.org/signatures/bukkit/RustyConnector.svg)](https://bstats.org/signatures/bukkit/RustyConnector.svg)
---
## 🛠 开发中的功能
- [ ] Rounded families (families built for round based gamemodes!) [__Under Development__]
- [ ] Player parties (join your friend's party and follow them around the network as they connect to different families!) [__Under Development__]
- [ ] Websocket as a backup for if Redis fails [__研究中__]
- [ ] Example RustyConnector Networks for Documentation Purposes [__Under Development__]
- [ ] Kubernetes自动伸缩 [__研究中__]
- [ ] RustyConnector API (可能暂时需要咕咕咕了!)
- [ ] Player routing based on client version (低优先级)
- [ ] Teleport players to specific coordinates on other servers (低优先级)

\*Checked boxes indicate that the feature has been added and is awaiting release in an official version. There is no promise on when these features may be added.
\*

---

# 📖 Wiki
### [Home](https://github.com/Aelysium-Group/rusty-connector/wiki)
### [FAQ](https://github.com/Aelysium-Group/rusty-connector/wiki#faq)
### [Getting Started | 快速入门](https://github.com/Aelysium-Group/rusty-connector/wiki/Getting-Started-(First-Time))
  - [Installation | 安装教程](https://github.com/Aelysium-Group/rusty-connector/wiki/Getting-Started-(First-Time))
  - [How it works | 插件是如何工作的?](https://github.com/Aelysium-Group/rusty-connector/wiki/Getting-Started-(First-Time)#how-it-works)
  - [Families | 家族配置](https://github.com/Aelysium-Group/rusty-connector/wiki/Family)
  - [Whitelists | 白名单](https://github.com/Aelysium-Group/rusty-connector/wiki/Whitelist)
  - [Load Balancing | 负载均衡](https://github.com/Aelysium-Group/rusty-connector/wiki/Family#load-balancing)
### [Commands | 插件指令](https://github.com/Aelysium-Group/rusty-connector/wiki/Commands)
### [Permissions | 插件权限](https://github.com/Aelysium-Group/rusty-connector/wiki/Permissions)
### [Configs | 插件配置](https://github.com/Aelysium-Group/rusty-connector/wiki/Config-Migration)
  - [RC-Velocity](https://github.com/Aelysium-Group/rusty-connector/wiki/Config-v2#rc-velocity)
  - [RC-Paper](https://github.com/Aelysium-Group/rusty-connector/wiki/Config-v2#rc-paper)
### [Config Migration | 配置迁移](https://github.com/Aelysium-Group/rusty-connector/wiki/Config-Migration)

---
# <a id=""></a>🌐 语言切换

Need to switch lang?

2023 © [Aelysium](https://www.aelysium.group)