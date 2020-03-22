package com.looyee.local.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyLocalHttpClientIdleStateTrigger extends SimpleChannelInboundHandler<HttpObject> {

    /*
    踩坑源码
    SimpleChannelInboundHandler
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean release = true;
        try {
            if (this.acceptInboundMessage(msg)) {
                this.channelRead0(ctx, msg);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (this.autoRelease && release) {
                这里会将msg 的引用计数释放一次，如果不进行处理，ctx.fireChannelRead(msg);
                会抛出 IllegalReferenceCountException 异常
                ReferenceCountUtil.release(msg);
            }
        }
    }
    */

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        // 这里选择继承 SimpleChannelInboundHandler 是为了演示一下当中可能有的坑
        ReferenceCountUtil.retain(httpObject);
        ctx.fireChannelRead(httpObject);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case WRITER_IDLE:
//                    log.info("客户端心跳写事件触发，维护服务端的长连接，并传递连接ClientId标识");
//                    ByteBuf buf = Unpooled.wrappedBuffer("I am ok".getBytes());
                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    response.headers().set("clientId", "looyee");
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                    ctx.writeAndFlush(response);
                    break;
            }
        }
    }

}
