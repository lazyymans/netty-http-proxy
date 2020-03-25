package com.looyee.origin;

import com.looyee.origin.handler.NettyOriginHttpServerInitializer;
import com.looyee.origin.innerserver.NettyOriginHttpInnerServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyOriginHttpServer {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 先启动对内部 Netty服务端
            final NettyOriginHttpInnerServer innerServer = new NettyOriginHttpInnerServer();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    innerServer.start();
                }
            }).start();

            // 创建服务器端的启动对象，配置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 使用链式编程来进行设置
            serverBootstrap.group(bossGroup, workerGroup)// 设置两个线程组
                    .channel(NioServerSocketChannel.class)// 使用NioServerSocketChannel 作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, false)
                    .childHandler(new NettyOriginHttpServerInitializer());// 给我们的workerGroup 的 EventLoop 对应的管道设置处理器

            // 绑定一个端口并且同步，生成一个ChannelFuture 对象
            // 启动服务器（并绑定端口）
            String serverHost = NettyProperties.BUNDLE.getString("netty.server.host");
            Integer serverPort = Integer.parseInt(NettyProperties.BUNDLE.getString("netty.server.port"));
            ChannelFuture future = serverBootstrap.bind(serverHost, serverPort).sync();
            log.info("启动外部服务成功");
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
