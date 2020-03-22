```
进入到netty-http-proxy-server 目录执行下面的打包命令
打包命令：mvn package assembly:single

云服务器运行命令
nohup java -Xmx128m -Xms128m -jar netty-http-proxy-server-1.0.jar &
```

