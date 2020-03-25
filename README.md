### 服务端启动相关

```
下载\拉取当前项目
进入到netty-http-proxy-server 目录执行下面的打包命令
打包命令：mvn package assembly:single

云服务器运行命令
nohup java -Xmx128m -Xms128m -jar netty-http-proxy-server-1.0.jar &

作者使用的是阿里云作为服务器，接下来需要对云服务做一些修改
1、在云服务器管理后台（阿里云控制台）加入安全组，你设置的netty-http-proxy-server 端口就是你需要加入安全组的端口
2、在云服务器加入防火墙开放的端口
	netty.server.port、netty.server.inner.port 就是你在云服务器要监听的那两个端口
	修改文件 /etc/sysconfig/iptables
	-A INPUT -p tcp -m state --state NEW -m tcp --dport ${netty.server.port} -j ACCEPT
	-A INPUT -p tcp -m state --state NEW -m tcp --dport ${netty.server.inner.port} -j ACCEPT
```

### 客户端启动相关

```
下载\拉取当前项目
配置好客户端的远程代理 netty.server.origin.host和 netty.server.inner.port
配置好客户端的代理本地程序 netty.proxy.host和 netty.proxy.port
启动 NettyLocalHttpClient main 方法
```

