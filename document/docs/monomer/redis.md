# Redis部署

## Docker部署

```shell
docker run -d --name redis -p 6379:6379 \
-v $PWD/voj/data/redis/data:/data \
--name voj-redis \
--restart="always" \
redis \
--requirepass "redis_password" 
```

## 常规部署

请自行探索。
