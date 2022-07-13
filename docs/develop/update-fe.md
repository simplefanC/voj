# 自定义前端

直接下载[voj-vue](https://github.com/simplefanc/voj-vue)

修改后，使用`npm run build`，生成一个dist文件夹，结构如下：

```
dist
├── index.html
├── favicon.ico
└── assets
    ├── css
    │   ├── ....
    ├── fonts
    │   ├── ....
    ├── img
    │   ├── ....
    ├── js
    │   ├── ....

....
....
```

将 `dist` 文件夹复制到服务器上某个目录下，比如 `/voj/www/html/dist`，然后修改 `docker-compose.yml`，在 `voj-frontend` 模块中的 `volumes` 中增加一行 `- /voj/www/html/dist:/usr/share/nginx/html` （冒号前面的请修改为实际的路径），然后 `docker-compose up -d` 即可。
