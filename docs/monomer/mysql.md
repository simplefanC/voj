# MySQL部署

## Docker部署

```shell
docker run -d --name voj-mysql \
-v $PWD/voj/data/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD="voj123456" \
-e TZ="Asia/Shanghai" \
-p 3306:3306 \
--restart="always" \
mysql:5.7
```

## 常规部署

请自行探索。
