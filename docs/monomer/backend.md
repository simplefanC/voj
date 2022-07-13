# 后端部署

## 前言

下载本项目，进入到当前文件夹执行打包命令

```shell
git clone https://github.com/simplefanc/voj-deploy.git && cd voj-deploy/src/backend
```

当前文件夹为打包`voj-backend`镜像的相关文件，将这些文件复制到同一个文件夹内，**然后打包[voj-backend](https://github.com/simplefanc/voj-springboot/tree/main/voj-backend)（SpringBoot项目）成jar包也放到当前文件夹**，之后执行以下命令进行打包成镜像

```shell
docker build -t voj-backend .
```

**项目依赖于`voj-redis`，`voj-nacos`，`voj-mysql`等镜像成功启动，以及根据前面三个镜像的配置修改环境参数才可正常启动**

docker-compose 启动

```yaml
version: "3"
services:
  voj-backend:
#    image: registry.cn-shanghai.aliyuncs.com/simplefanc/voj_backend
	image: voj-backend
    container_name: voj-backend
    restart: always
    depends_on:
      - voj-redis
      - voj-mysql
      - voj-nacos
    volumes:
      - ./voj/file:/voj/file
      - ./voj/testcase:/voj/testcase
      - ./voj/log/backend:/voj/log/backend
    environment:
      - TZ=Asia/Shanghai
      - BACKEND_SERVER_PORT=6688 # backend服务端口号
      - NACOS_URL=172.20.0.4:8848 # voj-nacos的url
      - NACOS_USERNAME=root # nacos的管理员账号
      - NACOS_PASSWORD=voj123456 # nacos的管理员密码
      - JWT_TOKEN_SECRET=default # 加密秘钥 默认则生成32位随机密钥
      - JWT_TOKEN_EXPIRE=86400 # token过期时间默认为24小时 86400s
      - JWT_TOKEN_FRESH_EXPIRE=43200 # token默认12小时可自动刷新
      - JUDGE_TOKEN=default # 调用判题服务器的token 默认则生成32位随机密钥
      - MYSQL_HOST=172.20.0.3 # voj-mysql的host
      - MYSQL_PUBLIC_HOST=172.20.0.3 # 如果判题服务是分布式，请提供当前mysql所在服务器的公网ip
      - MYSQL_PUBLIC_PORT=3306 # mysql主机端口号
      - MYSQL_PORT=3306 # mysql容器内端口号
      - MYSQL_DATABASE_NAME=voj # 改动需要修改voj-mysql镜像,默认为voj
      - MYSQL_USERNAME=root 
      - MYSQL_ROOT_PASSWORD=voj123456 # voj-mysql的root账号密码
      - EMAIL_SERVER_HOST=smtp.qq.com # 请使用邮件服务的域名或ip
      - EMAIL_SERVER_PORT=465 # 请使用邮件服务的端口号
      - EMAIL_USERNMAE=-your_email_username # 请使用对应邮箱账号
      - EMAIL_PASSWORD=-your_email_password # 请使用对应邮箱密码
      - REDIS_HOST=172.20.0.2 # voj-redis的host
      - REDIS_PORT=6379 # voj-redis的port
      - REDIS_PASSWORD=voj123456 #voj-redis的密码
    ports:
      - "6688:6688"
    networks:
      voj-network:
        ipv4_address: 172.20.0.5
        
  voj-redis:
    image: redis:5.0.9-alpine
    container_name: voj-redis
    restart: always
    volumes:
      - ./voj/data/redis/data:/data
    networks:
      voj-network:
        ipv4_address: 172.20.0.2
    ports:
      - "6379:6379"
    command: redis-server --requirepass "voj123456" --appendonly yes
        
  voj-mysql:
    image: registry.cn-shanghai.aliyuncs.com/simplefanc/voj_database
    container_name: voj-mysql
    restart: always
    volumes:
      - ./voj/data/mysql/data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=voj123456
      - TZ=Asia/Shanghai
      - NACOS_USERNAME=root
      - NACOS_PASSWORD=voj123456
    ports:
      - "3306:3306"
    networks:
      voj-network:
        ipv4_address: 172.20.0.3
      
  voj-nacos:
    image: nacos/nacos-server:1.4.2
    container_name: voj-nacos
    restart: always
    depends_on: 
      - voj-mysql
    environment:
      - JVM_XMX=384m
      - JVM_XMS=384m
      - JVM_XMN=192m
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=172.20.0.3
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=Hzh&hy2020
      - MYSQL_SERVICE_DB_NAME=nacos
      - NACOS_AUTH_ENABLE=true # 开启鉴权

networks:
   voj-network:
     driver: bridge
     ipam:
       config:
         - subnet: 172.20.0.0/16
```



## 文件介绍

### 1. check_nacos.sh

用于检测nacos是否启动完成，然后再执行启动backend

```shell
#!/bin/bash

while :
    do
        # 访问nacos注册中心，获取http状态码
        CODE=`curl -I -m 10 -o /dev/null -s -w %{http_code}  http://$NACOS_URL/nacos/index.html`
        # 判断状态码为200
        if [[ $CODE -eq 200 ]]; then
            # 输出绿色文字，并跳出循环
            echo -e "\033[42;34m nacos is ok \033[0m"
            break
        else
            # 暂停1秒
            sleep 1
        fi
    done

# while结束时，执行容器中的run.sh。
bash /run.sh
```

### 2. run.sh

启动backend的springboot jar包

```shell
#!/bin/sh

java -Djava.security.egd=file:/dev/./urandom -jar  /app.jar
```

### 3. Dockerfile

```dockerfile
FROM java:8

COPY *.jar /app.jar

COPY check_nacos.sh /check_nacos.sh

COPY run.sh /run.sh

ENV TZ=Asia/Shanghai

ENV BACKEND_SERVER_PORT=6688

VOLUME ["/voj/file","/voj/testcase"]

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

CMD ["bash","/check_nacos.sh"]

EXPOSE $BACKEND_SERVER_PORT

```
