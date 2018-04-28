//package jef.net;
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelPipeline;
//
//import java.net.InetSocketAddress;
//import java.nio.channels.Channels;
//import java.util.concurrent.Executors;
//
//public class NettyTest {
//	public void run() {
//	    // Configure the server.
//	    ServerBootstrap bootstrap = new ServerBootstrap(
//	            new NioServerSocketChannelFactory(
//	                    Executors.newCachedThreadPool(),
//	                    Executors.newCachedThreadPool()));
//
//	    // Set up the pipeline factory.
//	    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
//	        public ChannelPipeline getPipeline() throws Exception {
//	            return Channels.pipeline(new EchoServerHandler());
//	        }
//	    });
//
//	    // Bind and start to accept incoming connections.
//	    bootstrap.bind(new InetSocketAddress(port));
//	}
//	这里EchoServerHandler是其业务逻辑的实现者，大致代码如下：
//
//
//	public class EchoServerHandler extends SimpleChannelUpstreamHandler {
//
//	    @Override
//	    public void messageReceived(
//	            ChannelHandlerContext ctx, MessageEvent e) {
//	        // Send back the received message to the remote peer.
//	        e.getChannel().write(e.getMessage());
//	    }
//	}
//}
