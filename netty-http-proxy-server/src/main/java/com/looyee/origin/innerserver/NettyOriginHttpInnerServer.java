package com.looyee.origin.innerserver;

import com.looyee.origin.NettyProperties;
import com.looyee.origin.innerserver.handler.NettyOriginHttpInnerServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyOriginHttpInnerServer {

    public static void main(String[] args) throws InterruptedException {

        NettyOriginHttpInnerServer nettyOriginHttpInnerServer = new NettyOriginHttpInnerServer();
        nettyOriginHttpInnerServer.start();

    }


    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建服务器端的启动对象，配置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 使用链式编程来进行设置
            serverBootstrap.group(bossGroup, workerGroup)// 设置两个线程组
                    .channel(NioServerSocketChannel.class)// 使用NioServerSocketChannel 作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new NettyOriginHttpInnerServerInitializer());// 给我们的workerGroup 的 EventLoop 对应的管道设置处理器

            // 绑定一个端口并且同步，生成一个ChannelFuture 对象
            // 启动服务器（并绑定端口）
            String serverInnerHost = NettyProperties.BUNDLE.getString("netty.server.inner.host");
            Integer serverInnerPort = Integer.parseInt(NettyProperties.BUNDLE.getString("netty.server.inner.port"));
            ChannelFuture future = serverBootstrap.bind(serverInnerHost, serverInnerPort).sync();
            log.info("启动内部服务成功");
            // 对关闭通道进行监听
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
