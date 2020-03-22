package com.looyee.origin.innerserver.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NettyOriginHttpInnerServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static ConcurrentHashMap<String, ChannelHandler> channelMaps = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<ChannelId, String> channels = new ConcurrentHashMap<>();
    public ChannelHandlerContext context;
    public ReentrantLock reentrantLock;
    public Condition condition;
    public FullHttpResponse proxyResult;
//    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) httpObject;
            if (response.headers().contains("clientId")) {
                String clientId = response.headers().get("clientId");
                if (!channelMaps.containsKey(clientId)) {
                    channels.put(ctx.channel().id(), clientId);
                    context = ctx;
                    channelMaps.put(clientId, this);
                }
            } else {
                reentrantLock.lock();
                ReferenceCountUtil.retain(httpObject);
                proxyResult = (FullHttpResponse) httpObject;
                condition.signal();
                reentrantLock.unlock();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("客户端channel {} 下线", ctx.channel().id());
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ChannelId id = ctx.channel().id();
        log.info("客户端channel {} 下线", id);
        String clientId = channels.get(id);
        channelMaps.remove(clientId);
        channels.remove(id);
        this.context = null;
        this.reentrantLock = null;
        this.condition = null;
        this.proxyResult = null;
    }

    public ChannelHandlerContext getContext() {
        return this.context;
    }

    public void setLock(ReentrantLock reentrantLock, Condition condition) {
        this.reentrantLock = reentrantLock;
        this.condition = condition;
    }

    public FullHttpResponse getProxyResult() {
        return this.proxyResult;
    }

    public void flushProxyResult() {
        this.proxyResult = null;
    }
}
