package com.looyee.origin.innerserver.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyOriginHttpInnerServerIdleStateTrigger extends SimpleChannelInboundHandler<HttpObject> {

    /*
    踩坑源码
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

    /**
     * @param ctx
     * @param httpObject
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
//        log.info("服务端接收心跳事件");
        // 这里选择继承 SimpleChannelInboundHandler 是为了演示一下当中可能有的坑
        ReferenceCountUtil.retain(httpObject);
        ctx.fireChannelRead(httpObject);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    log.info("本地服务端触发心跳剔除机制, 在规定时间内没有收到客户端的上行数据, 主动断开连接");
                    // 在规定时间内没有收到客户端的上行数据, 主动断开连接
                    ctx.close();
                    break;
            }
        }
    }

}
