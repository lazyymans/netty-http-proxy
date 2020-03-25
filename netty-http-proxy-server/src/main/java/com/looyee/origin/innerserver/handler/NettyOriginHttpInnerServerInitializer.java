package com.looyee.origin.innerserver.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyOriginHttpInnerServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 这里为什么不使用这两个类来对客户端、服务端处理，在实验的过程中只能正向使用
        // 无法反向使用，比如在 Server端使用 HttpClientCodec、在 Client端使用 HttpServerCodec
        // 可能这种用法很奇怪，这里是做一个快速版的 HTTP 内网穿透服务
        // pipeline.addLast("HttpClientCodec", new HttpClientCodec());
        // pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        // 当前程序在进行心跳机制响应事件的时候，必须设置的响应头，不设置响应头无法心跳成功，这里本人没有去研究为什么
        // response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        // 使用 HttpResponseDecoder进行解码
        pipeline.addLast("HttpResponseDecoder", new HttpResponseDecoder());
        // 使用 HttpRequestEncoder进行编码
        pipeline.addLast("HttpRequestEncoder", new HttpRequestEncoder());
        // 2m
        pipeline.addLast("HttpObjectAggregator", new HttpObjectAggregator(2 * 1024 * 1024));
        pipeline.addLast("IdleStateHandler", new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast("NettyOriginHttpInnerServerIdleStateTrigger", new NettyOriginHttpInnerServerIdleStateTrigger());
        pipeline.addLast("NettyOriginHttpInnerServerHandler", new NettyOriginHttpInnerServerHandler());
    }
}
