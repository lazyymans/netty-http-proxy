package com.looyee.local;

import com.looyee.local.handler.NettyLocalHttpClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyLocalHttpClient {

    public static void main(String[] args) {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors)// 设置线程组
                    .channel(NioSocketChannel.class)// 设置客户端通道的实现类（反射）
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new NettyLocalHttpClientInitializer());

            // 关于ChannelFuture 要分析，涉及到netty 的一部模型
            String serverHost = NettyProperties.BUNDLE.getString("netty.server.origin.host");
            Integer serverPort = Integer.parseInt(NettyProperties.BUNDLE.getString("netty.server.origin.port"));
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(serverHost, serverPort)).sync();

            // 启动立即在服务端注册 clientId
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set("clientId", "looyee");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            channelFuture.channel().writeAndFlush(response);

            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }

}
