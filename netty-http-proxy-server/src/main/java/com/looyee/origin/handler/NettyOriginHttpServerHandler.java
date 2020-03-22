package com.looyee.origin.handler;

import com.looyee.origin.innerserver.handler.NettyOriginHttpInnerServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NettyOriginHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private ReentrantLock reentrantLock;
    private Condition condition;

    public NettyOriginHttpServerHandler(ReentrantLock reentrantLock, Condition condition) {
        this.reentrantLock = reentrantLock;
        this.condition = condition;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        URI uri = null;
        if (httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            uri = new URI(httpRequest.uri());
            log.info("", uri);
            if ("/favicon.ico".equals(uri.getPath())) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8; text/plain");
                ctx.writeAndFlush(response);
                return;
            }
        }
        log.info("用户请求 uri = {}", uri.getPath());

        NettyOriginHttpInnerServerHandler proxyHandler = (NettyOriginHttpInnerServerHandler) NettyOriginHttpInnerServerHandler.channelMaps.get("looyee");
        if (proxyHandler != null && proxyHandler.getContext() != null) {
            proxyHandler.setLock(reentrantLock, condition);
            reentrantLock.lock();
            ChannelHandlerContext proxyContext = proxyHandler.getContext();
            ReferenceCountUtil.retain(httpObject);
            proxyContext.writeAndFlush(httpObject);
            condition.await();
            FullHttpResponse proxyResult = proxyHandler.getProxyResult();
            reentrantLock.unlock();
            ctx.writeAndFlush(proxyResult);
        } else {
            ByteBuf buf = Unpooled.wrappedBuffer("代理服务失败，本地客户端未启动".getBytes());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8; text/plain");
            ctx.writeAndFlush(response);
        }


    }


    /**
     * 异常处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
