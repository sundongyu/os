# mysql 8.0安装

## 1、修改初始密码

``` shell
ALTER USER 'root'@'localhost' IDENTIFIED BY 'MyNewPass4!';
```

## 2、配置远程登录

创建新用户，mysql 8.0不能给自己授权

``` shell
CREATE user 'root'@'%' IDENTIFIED BY 'test';
CREATE user 'sdy'@'%' IDENTIFIED BY 'test';
```

## 3、授权用户权限

``` shell
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'sdy'@'%';
```

## 4、修改密码

``` shell
ALTER user 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'test';
ALTER user 'sdy'@'%' IDENTIFIED WITH mysql_native_password BY 'test';
```

## 5、刷新保存重启

``` shell
FLUSH PRIVILEGES;
systemctl restart mysqld
```
