# Virtual Online Judge（VOJ）

[![Java](https://img.shields.io/badge/Java-11-informational)](https://openjdk.java.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.3-success)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.1-success)](https://spring.io/projects/spring-cloud)
[![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2021.0.1.0-success)](https://spring.io/projects/spring-cloud-alibaba)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.19-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-5.0.9-red)](https://redis.io/)
[![Vue](https://img.shields.io/badge/Vue-2.6.11-success)](https://cn.vuejs.org/)

## 简介

VOJ 是基于微服务、前后端分离的高性能在线评测系统。采用现阶段流行技术实现，采用 Docker 容器化部署。

## 概览

+ 基于 Docker，真正一键部署
+ 前后端分离，模块化编程
+ 微服务，支持分布式判题
+ 拥有**本地判题**服务，同时支持其它知名 OJ (HDU、POJ、Codeforces、AtCoder...) 的**远程判题**
+ ACM/OI 两种比赛模式、完善的比赛功能（打星队伍、关注队伍、外榜）
+ 完善的判题模式（普通测评、特殊测评、交互测评）
+ 支持私有训练、公开训练（题单）
+ 更细致的权限划分，超级管理员、题目管理员、普通管理员各司其职
+ 丰富的可视化图表，一图胜千言
+ 支持 Template Problem，可以添加函数题甚至填空题
+ 多语言支持：`C`, `C++`, `Java`, `Python`, `C#`，`GoLang`
+ Markdown & LaTeX 支持

## 项目源码

+ 后端源码：[https://github.com/simplefanC/voj](https://github.com/simplefanC/voj)
+ 前端源码：[https://github.com/simplefanC/voj-vue](https://github.com/simplefanC/voj-vue)
+ 判题沙箱：[https://github.com/criyle/go-judge](https://github.com/criyle/go-judge)

## 项目文档

项目文档地址：[https://github.com/simplefanC/voj/wiki](https://github.com/simplefanC/voj/wiki)

## 项目结构

```
voj
├── voj-common -- 工具类及通用代码
├── voj-backend -- 业务服务模块
└── voj-judger -- 评测服务模块
```

## 技术选型

| 技术                 | 说明                | 官网                                            |
| -------------------- | ------------------- | ----------------------------------------------- |
| Spring Boot          | 容器+MVC框架        | https://spring.io/projects/spring-boot          |
| Spring Cloud         | 微服务框架          | https://spring.io/projects/spring-cloud         |
| Spring Cloud Alibaba | 微服务框架          | https://spring.io/projects/spring-cloud-alibaba |
| MyBatis-Plus         | ORM框架             | https://baomidou.com                            |
| Druid                | 数据库连接池        | https://github.com/alibaba/druid                |
| Redis                | 分布式缓存          | https://redis.io                                |
| Shiro                | 认证和授权框架      | https://shiro.apache.org                        |
| JWT                  | JWT登录支持         | https://github.com/jwtk/jjwt                    |
| Hibernator-Validator | 验证框架            | http://hibernate.org/validator                  |
| EasyExcel            | JAVA解析Excel工具   | https://github.com/alibaba/easyexcel            |
| PageHelper           | MyBatis物理分页插件 | https://github.com/pagehelper/Mybatis-PageHelper  |
| Hutool               | Java工具类库        | https://github.com/looly/hutool                 |
| Lombok               | 简化对象封装工具    | https://github.com/rzwitserloot/lombok          |
| Swagger-UI           | 文档生成工具        | https://github.com/swagger-api/swagger-ui       |
| Nginx                | 静态资源服务器      | https://www.nginx.com                          |
| Docker               | 应用容器引擎        | https://www.docker.com     

## 部署

快速部署：[基于 Docker Compose 部署](https://github.com/simplefanC/voj/wiki/deploy)

部署仓库：[https://github.com/simplefanC/voj-deploy](https://github.com/simplefanC/voj-deploy)
