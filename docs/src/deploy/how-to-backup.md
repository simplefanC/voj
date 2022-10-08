# 如何备份

### 1. 单体部署

部署脚本（`docker-compose.yml`）同目录的`voj`文件夹中，存储了本系统的全部数据：

```html
voj
├── file   		# 存储了上传的图片、上传的临时题目数据、markdown引用的文件等文件
├── judge  		# 存储了每个提交题目的评测过程产生的数据
├── log    		# 存储了voj-backend项目的运行日志
├── testcase    # 存储了题目的评测数据
└── data        
    ├── mysql
    │   ├── data # 存储了MySQL数据库的数据
    ├── redis
    │   ├── data # 存储了redis产生的快照数据
```

如果需要备份，只需将该`voj`文件夹复制一份即可，在新的机器上重新部署VOJ时，将该文件夹放置与`docker-compose.yml`同目录下，使用`docker-compose up -d`即可启动恢复原来的数据。



### 2. 分布式部署

- 主服务器（运行`voj-backend`的服务器）

  部署脚本（`docker-compose.yml`）同目录的`voj`文件夹中，存储了本系统主服务器的全部数据：

  ```html
  voj
  ├── file   		# 存储了上传的图片、上传的临时题目数据、markdown引用的文件等文件
  ├── log    		# 存储了voj-backend项目的运行日志
  ├── testcase    # 存储了题目的评测数据
  └── data        
      ├── mysql
      │   ├── data # 存储了MySQL数据库的数据
      ├── redis
      │   ├── data # 存储了redis产生的快照数据
  ```

- 判题服务器（运行`voj-judger`的服务器）

  部署脚本（`docker-compose.yml`）同目录的`voj`文件夹中，存储了本系统判题服务器的全部数据：

  ```html
  judge
  ├── run  		# 存储了每个提交题目的评测过程产生的数据
  ├── log    		# 存储了voj-judger项目的运行日志
  ├── testcase    # 存储了题目的评测数据(每100s从主服务器同步)
  ├── spj         # 存储了SPJ的代码
  ```

那么，主要要备份的还是**主服务器**的数据，只需将该`voj`文件夹复制一份即可，在新的机器上重新部署新的voj的时候，将该文件夹放置与`docker-compose.yml`一个目录下，使用`docker-compose up -d`即可启动恢复原来的数据。

