# 快速部署
**前提：已经在上一步准备好 Docker 与 Docker Compose**
:::danger
注意：如果正式部署运行 VOJ，请修改默认配置的密码，例如MySQL、Nacos的密码！！！  
**使用默认密码可能会导致数据泄露，网站极其不安全！**
:::

## 一、单机部署

1. 选择好需要安装的位置，运行下面命令

   ```shell
   git clone https://github.com/simplefanc/voj-deploy.git && cd voj-deploy
   ```

2. 进入文件夹，使用docker-compose启动各容器服务

   ```shell
   cd standAlone
   ```

   `standAlone`文件夹文件有以下：

   ```bash
   ├── docker-compose.yml
   ├── .env
   ```

   主要配置请见`.env`文件，内容如下：

   > 注意：各服务ip最好不改动，保持处于172.20.0.0/16网段的docker network

   ```properties
   # VOJ全部数据存储的文件夹位置（默认当前路径生成voj文件夹）
    VOJ_DATA_DIRECTORY=./voj
    
    # Redis的配置
    REDIS_HOST=172.20.0.2
    REDIS_PORT=6379
    
    # MySQL的配置
    MYSQL_HOST=172.20.0.3
    # 如果判题服务是分布式，请提供当前MySQL所在服务器的公网ip
    MYSQL_PUBLIC_HOST=172.20.0.3
    MYSQL_PORT=3306
    MYSQL_ROOT_PASSWORD=voj123456
    
    # Nacos的配置
    NACOS_HOST=172.20.0.4
    NACOS_PORT=8848
    NACOS_USERNAME=nacos
    NACOS_PASSWORD=nacos
    
    # backend后端服务的配置
    BACKEND_HOST=172.20.0.5
    BACKEND_PORT=6688
    # JWT加密秘钥，default则生成32位随机密钥
    JWT_TOKEN_SECRET=default
    # token过期时间默认为24小时（86400s）
    JWT_TOKEN_EXPIRE=86400
    # JWT默认12小时自动刷新
    JWT_TOKEN_FRESH_EXPIRE=43200
    # 调用判题服务器的token，default则生成32位随机密钥
    JUDGE_TOKEN=default
    # 请使用邮件服务的域名或ip
    EMAIL_SERVER_HOST=smtp.qq.com
    EMAIL_SERVER_PORT=465
    EMAIL_USERNAME=your_email_username
    EMAIL_PASSWORD=your_email_password
    
    # 判题服务的配置
    JUDGE_SERVER_IP=172.20.0.7
    JUDGE_SERVER_PORT=8088
    JUDGE_SERVER_NAME=judger-alone
    # -1表示可接收最大判题任务数为：cpu核心数+1
    MAX_TASK_NUM=-1
    # 当前判题服务器是否开启远程虚拟判题功能
    REMOTE_JUDGE_OPEN=true
    # -1表示可接收最大远程判题任务数为：cpu核心数*2+1
    REMOTE_JUDGE_MAX_TASK_NUM=-1
    # 默认沙盒并行判题程序数为：cpu核心数
    PARALLEL_TASK=default
    
    # docker network的配置
    SUBNET=172.20.0.0/16
   ```
   

:::tip   
提示：如果服务器的内存在4G或4G以上，请去掉JVM限制才能提高并发量，操作如下：

- 注释或删除`docker-compose.yml`中`voj-backend`模块和`voj-judger`模块`environment`参数`JAVA_OPTS`

:::

3. 如果不改动，则以默认参数启动（**正式部署请修改默认配置的密码！**）

   ```shell
   docker-compose up -d
   ```
4. 对依赖服务 MySQL 进行以下设置（分布式部署主服务器时同理）
    1. 将 voj.sql 和 nacos_config.sql 文件拷贝到容器/目录下：
       ```
       docker cp ../sql/voj.sql voj-mysql:/
       docker cp ../sql/nacos_config.sql voj-mysql:/
       ```
    2. 进入 voj-mysql 容器并执行如下操作：
        ```
        # 进入mysql容器
        docker exec -it voj-mysql /bin/bash
        # 连接到mysql服务
        mysql -uroot -pvoj123456
        # 导入sql脚本
        source /voj.sql
        source /nacos_config.sql
        ```
    3. 退出 voj-mysql 容器。
    
5. docker-compose restart
   
   等待命令执行完毕后，查看容器状态

   ```shell
   docker ps -a
   ```

   当看到所有的容器的状态status都为`UP`或`healthy`就代表 OJ 已经启动成功。

   以下默认参数说明
   :::warning
   - 默认超级管理员账号与密码：root / voj123456

   - 默认 MySQL 账号与密码：root / voj123456（**正式部署请修改**）

   - 默认 Nacos 管理员账号与密码：root / voj123456（**正式部署请修改**）

   - 默认不开启 https，开启需修改文件同时提供证书文件

   - 判题并发数默认：cpu核心数+1

   - 默认开启vj判题，需要手动修改添加账号与密码，如果不添加不能vj判题！

   - vj判题并发数默认：cpu核心数*2+1
     

   :::

   

**登录root账号到后台查看服务状态以及到`http://ip/admin/conf`修改服务配置！**

<u>注意：网站的注册及用户账号相关操作需要邮件系统，所以请在系统配置中配置自己的SMTP邮件服务。</u>

**(如果已经在启动在.env文件配置了邮件服务即不用再次修改)**

```bash
Host: smtp.qq.com
Port: 465
Username: 邮箱账号
Password: 开启SMTP服务后生成的随机授权码
```



## 二、分布式部署

:::tip
主服务器（运行Nacos, backend, frontend, Redis）的服务器防火墙请开8848(Nacos), 3306(MySQL), 873(Rsync)端口

从服务器（运行judger）的服务器防火墙请开8088端口
:::

1. 选择好需要安装的位置，运行下面命令

   ```shell
   git clone https://github.com/simplefanc/voj-deploy.git && cd voj-deploy
   ```

2. 进入文件夹

   ```shell
   cd distributed
   ```

   `distributed`文件夹有以下：

   ```bash
   ├── judger
   ├── main
   ```

3. 首先部署主服务，即数据后台服务（DataBackup）

   ```shell
   cd main
   ```

   该文件夹下有：

   ```bash
   ├── docker-compose.yml
   ├── .env
   ```

   修改`.env`文件中的配置

   ```shell
   vim .env
   ```

   > 注意：各服务ip最好不改动，保持处于172.20.0.0/16网段的docker network

   ```properties
    # voj全部数据存储的文件夹位置（默认当前路径生成voj文件夹）
    VOJ_DATA_DIRECTORY=./voj
    
    # Redis的配置
    REDIS_HOST=172.20.0.2
    REDIS_PORT=6379
    
    # MySQL的配置
    MYSQL_HOST=172.20.0.3
    # 请提供当前MySQL所在服务器的公网ip
    MYSQL_PUBLIC_HOST=
    MYSQL_PORT=3306
    MYSQL_ROOT_PASSWORD=voj123456
    
    # Nacos的配置
    NACOS_HOST=172.20.0.4
    NACOS_PORT=8848
    NACOS_USERNAME=nacos
    NACOS_PASSWORD=nacos
    
    # backend后端服务的配置
    BACKEND_HOST=172.20.0.5
    BACKEND_PORT=6688
    # JWT加密秘钥，默认则生成32位随机密钥
    JWT_TOKEN_SECRET=default
    # JWT过期时间默认为24小时(86400s)
    JWT_TOKEN_EXPIRE=86400
    # JWT默认12小时可自动刷新
    JWT_TOKEN_FRESH_EXPIRE=43200
    # 调用判题服务器的token，默认则生成32位随机密钥
    JUDGE_TOKEN=default
    
    # 请使用邮件服务的域名或ip
    EMAIL_SERVER_HOST=smtp.qq.com
    EMAIL_SERVER_PORT=465
    EMAIL_USERNAME=your_email_username
    EMAIL_PASSWORD=your_email_password
    
    # 评测数据同步的配置
    # 请修改数据同步密码
    RSYNC_PASSWORD=voj123456
    
    # docker network的配置
    SUBNET=172.20.0.0/16
   ```
   
   配置修改保存后，当前路径下启动该服务
   
   ```shell
   docker-compose up -d
   ```
   
   等待命令执行完毕后，查看容器状态
   
   ```shell
   docker ps -a
   ```
   
   当看到所有的容器的状态status都为`UP`或`healthy`就代表 OJ 已经启动成功。
   
   
   
4. 接着，在另一台服务器上，依旧git clone该文件夹下来，然后进入`judger`文件夹，修改`.env`的配置

   ```properties
    # voj全部数据存储的文件夹位置（默认当前路径生成judge文件夹）
    VOJ_JUDGESERVER_DATA_DIRECTORY=./judge
    
    # Nacos的配置
    # 修改为Nacos所在服务的公网ip
    NACOS_HOST=
    # 修改为Nacos启动端口号，默认为8848
    NACOS_PORT=8848
    # 修改为Nacos的管理员账号
    NACOS_USERNAME=nacos
    # 修改为Nacos的管理员密码
    NACOS_PASSWORD=nacos
    
    # judgeserver的配置
    # 修改本服务器公网ip
    JUDGE_SERVER_IP=
    JUDGE_SERVER_PORT=8088
    JUDGE_SERVER_NAME=judger-1
    # -1表示可接收最大判题任务数为：cpu核心数+1
    MAX_TASK_NUM=-1
    # 当前判题服务器是否开启远程虚拟判题功能
    REMOTE_JUDGE_OPEN=true
    # -1表示可接收最大远程判题任务数为：cpu核心数*2+1
    REMOTE_JUDGE_MAX_TASK_NUM=-1
    # 默认沙盒并行判题程序数为cpu核心数
    PARALLEL_TASK=default
    
    # rsync评测数据同步的配置
    # 写入主服务器公网ip
    RSYNC_MASTER_ADDR=
    # 与主服务器的rsync密码一致
    RSYNC_PASSWORD=voj123456
   ```

   配置修改保存后，当前路径下启动该服务

   ```shell
   docker-compose up -d
   ```
   :::tip
   提示：需要开启多台判题机，就如当前第4步的操作一样，在每台服务器上执行以上的操作即可。
   :::
5. 两个服务都启动完成，在浏览器输入主服务ip或域名进行访问，登录root账号到后台查看服务状态。
