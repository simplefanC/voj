# 开启HTTPS

- 单机部署：

  提供`server.pem`和`server.key`证书与密钥文件放置`/standAlone`目录下，与`docker-compose.yml`和`.env`文件放于同一位置，然后修改`docker-compose.yml`中的`voj-frontend`的配置

- 分布式部署：

  提供`server.pem`和`server.key`证书与密钥文件放置`/distributed/main`目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的`voj-frontend`的配置

```yaml
voj-frontend:
  image: registry.cn-shanghai.aliyuncs.com/simplefanc/voj_frontend
  container_name: voj-frontend
  restart: always
  volumes:
    - ./html/dist:/usr/share/nginx/html
    # 开启https，请提供证书
    - ./server.pem:/etc/nginx/conf.d/cert/server.pem
    - ./server.key:/etc/nginx/conf.d/cert/server.key
  environment:
    - SERVER_NAME=localhost # 域名或localhost(本地)
    - BACKEND_SERVER_HOST=${BACKEND_HOST:-172.20.0.5} # backend后端服务地址
    - BACKEND_SERVER_PORT=${BACKEND_PORT:-6688} # backend后端服务端口号
    - USE_HTTPS=true # 使用https请设置为true
  ports:
    - "80:80"
    - "443:443"
  networks:
    voj-network:
      ipv4_address: 172.20.0.6
```

