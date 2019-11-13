## 前言
现在网上有很多关于使用Netty来构建RPC框架的例子，为什么我这里还要写一篇文章进行论述呢，我很清楚我可能没有写得他们那么好。之所以还要写，有两点原因:
- 一是因为学过Netty之后，还需要去不断实践才能更好的把握Netty的用法，显然，基于Netty实现RPC框架是一个很好的做法；
- 二是因为目前市面上有很多RPC框架，比如Dubbo，这些框架通讯底层都是Netty，所以说通过这个例子，也可以更好的去体验RPC的设计。


下面我将从以下几点阐述如何基于Netty实现简易的RPC框架：
- RPC是什么？
- 实现RPC框架需要关注哪些方面 ？
- 使用Netty如何实现？


## RPC是什么？

RPC，远程过程调用，可以做到像本地调用一样调用远程服务，是一种进程间的通信方式，概念想必大家都很清楚，在看到58沈剑写的[RPC文章](https://www.w3cschool.cn/architectroad/architectroad-rpc-framework.html) 之后，意识到其实我们可以换一种思考方式去理解RPC，也就是从本地调用出发，进而去推导RPC调用

![rpc_58](https://pjmike-1253796536.cos.ap-beijing.myqcloud.com/%E5%88%86%E5%B8%83%E5%BC%8F/RPC/rpc_58.png)


### 1. 本地函数调用

本地函数是我们经常碰到的，比如下面示例：
```
public String sayHello(String name) {
    return "hello, " + name;
}
```
我们只需要传入一个参数，调用sayHello方法就可以得到一个输出，也就是输入参数——>方法体——>输出，入参、出参以及方法体都在同一个进程空间中，这就是**本地函数调用**

### 2. Socket通信
那有没有办法实现不同进程之间通信呢？调用方在进程A，需要调用方法A，但是方法A在进程B中

![rpc_2](https://pjmike-1253796536.cos.ap-beijing.myqcloud.com/%E5%88%86%E5%B8%83%E5%BC%8F/RPC/rpc_58_2.png)

最容易想到的方式就是使用Socket通信，使用Socket可以完成跨进程调用，我们需要约定一个进程通信协议，来进行传参，调用函数，出参。写过Socket应该都知道，Socket是比较原始的方式，我们需要更多的去关注一些细节问题，比如参数和函数需要转换成字节流进行网络传输，也就是序列化操作，然后出参时需要反序列化；使用socket进行底层通讯，代码编程也比较容易出错。

如果一个调用方需要关注这么多问题，那无疑是个灾难，所以有没有什么简单方法，让我们的调用方不需要关注细节问题，让调用方像调用本地函数一样，只要传入参数，调用方法，然后坐等返回结果就可以了呢？

### 3. RPC框架

RPC框架就是用来解决上面的问题的，它能够让调用方像调用本地函数一样调用远程服务，底层通讯细节对调用方是透明的，将各种复杂性都给屏蔽掉，给予调用方极致体验。

![rpc_3](https://pjmike-1253796536.cos.ap-beijing.myqcloud.com/%E5%88%86%E5%B8%83%E5%BC%8F/RPC/rpc_58_3.png)



## RPC调用需要关注哪些方面

前面就已经说到RPC框架，让调用方像调用本地函数一样调用远程服务，那么如何做到这一点呢？

在使用的时候，调用方是直接调用本地函数，传入相应参数，其他细节它不用管，至于通讯细节交给RPC框架来实现。实际上RPC框架采用代理类的方式，具体来说是动态代理的方式，在运行时动态创建新的类，也就是代理类，在该类中实现通讯的细节问题，比如参数**序列化**。


当然不光是序列化，我们还需要**约定一个双方通信的协议格式**，规定好协议格式，比如请求参数的数据类型，请求的参数，请求的方法名等，这样根据格式进行序列化后进行网络传输，然后服务端收到请求对象后按照指定格式进行解码，这样服务端才知道具体该调用哪个方法，传入什么样的参数。


刚才又提到**网络传输**，RPC框架重要的一环也就是网络传输，服务是部署在不同主机上的，如何高效的进行网络传输，尽量不丢包，保证数据完整无误的快速传递出去？实际上，就是利用我们今天的主角——**Netty**，Netty是一个高性能的网络通讯框架，它足以胜任我们的任务。



前面说了这么多，再次总结下一个RPC框架需要重点关注哪几个点：
- 代理 （动态代理）
- 通讯协议
- 序列化
- 网络传输

当然一个优秀的RPC框架需要关注的不止上面几点，只不过本篇文章旨在做一个简易的RPC框架，理解了上面关键的几点就够了

![rpc_4](https://pjmike-1253796536.cos.ap-beijing.myqcloud.com/%E5%88%86%E5%B8%83%E5%BC%8F/RPC/rpc_58_4.png)



## 基于Netty实现RPC框架

终于到了本文的重头戏了，我们将根据实现RPC需要关注的几个要点（代理、序列化、协议、编解码），使用Netty进行逐一实现

### 1. Protocol（协议）
首先我们需要确定通信双方的协议格式，请求对象和响应对象


请求对象：
```java
@Data
@ToString
public class RpcRequest {
    /**
     * 请求对象的ID
     */
    private String requestId;
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 入参
     */
    private Object[] parameters;
}

```
- 请求对象的ID是客户端用来验证服务器请求和响应是否匹配


响应对象：
```java
@Data
public class RpcResponse {
    /**
     * 响应ID
     */
    private String requestId;
    /**
     * 错误信息
     */
    private String error;
    /**
     * 返回的结果
     */
    private Object result;
}
```


### 2. 序列化

市面上序列化协议很多，比如jdk自带的，Google的protobuf，kyro、Hessian等，只要不选择jdk自带的序列化方法，（因为其性能太差，序列化后产生的码流太大），其他方式其实都可以，这里为了方便起见，**选用JSON作为序列化协议，使用fastjson作为JSON框架**

为了后续扩展方便，先定义序列化接口：
```java
public interface Serializer {
    /**
     * java对象转换为二进制
     *
     * @param object
     * @return
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * 二进制转换成java对象
     *
     * @param clazz
     * @param bytes
     * @param <T>
     * @return
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
```
因为我们采用JSON的方式，所以定义JSONSerializer的实现类:
```java
public class JSONSerializer implements Serializer{

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
```
如果后续要使用其他序列化方式，可以自行实现序列化接口


### 3. 编解码器
约定好协议格式和序列化方式之后，我们还需要编解码器，编码器将请求对象转换为适合于传输的格式（一般来说是字节流），而对应的解码器是将网络字节流转换回应用程序的消息格式。


编码器实现：
```java
public class RpcEncoder extends MessageToByteEncoder {
    private Class<?> clazz;
    private Serializer serializer;

    public RpcEncoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        if (clazz != null && clazz.isInstance(msg)) {
            byte[] bytes = serializer.serialize(msg);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
```
解码器实现：
```java
public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> clazz;
    private Serializer serializer;

    public RpcDecoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //因为之前编码的时候写入一个Int型，4个字节来表示长度
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        //标记当前读的位置
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        //将byteBuf中的数据读入data字节数组
        byteBuf.readBytes(data);
        Object obj = serializer.deserialize(clazz, data);
        list.add(obj);
    }
}
```

### 4. Netty 客户端

下面来看看Netty客户端是如何实现的，也就是如何使用Netty开启客户端。

实际上，熟悉Netty的朋友应该都知道，我们需要注意以下几点：
- 编写启动方法，指定传输使用Channel
- 指定ChannelHandler，对网络传输中的数据进行读写处理
- 添加编解码器
- 添加失败重试机制
- 添加发送请求消息的方法


下面来看具体的实现代码：
```java
@Slf4j
public class NettyClient {
    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private ClientHandler clientHandler;
    private String host;
    private Integer port;
    private static final int MAX_RETRY = 5;
    public NettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
    public void connect() {
        clientHandler = new ClientHandler();
        eventLoopGroup = new NioEventLoopGroup();
        //启动类
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                //指定传输使用的Channel
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //添加编码器
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JSONSerializer()));
                        //添加解码器
                        pipeline.addLast(new RpcDecoder(RpcResponse.class, new JSONSerializer()));
                        //请求处理类
                        pipeline.addLast(clientHandler);
                    }
                });
        connect(bootstrap, host, port, MAX_RETRY);
    }

    /**
     * 失败重连机制，参考Netty入门实战掘金小册
     *
     * @param bootstrap
     * @param host
     * @param port
     * @param retry
     */
    private void connect(Bootstrap bootstrap, String host, int port, int retry) {
        ChannelFuture channelFuture = bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("连接服务端成功");
            } else if (retry == 0) {
                log.error("重试次数已用完，放弃连接");
            } else {
                //第几次重连：
                int order = (MAX_RETRY - retry) + 1;
                //本次重连的间隔
                int delay = 1 << order;
                log.error("{} : 连接失败，第 {} 重连....", new Date(), order);
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
            }
        });
        channel = channelFuture.channel();
    }

    /**
     * 发送消息
     *
     * @param request
     * @return
     */
    public RpcResponse send(final RpcRequest request) {
        try {
            channel.writeAndFlush(request).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientHandler.getRpcResponse(request.getRequestId());
    }
    @PreDestroy
    public void close() {
        eventLoopGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }
}

```

我们对于数据的处理重点在于ClientHandler类上，它继承了ChannelDuplexHandler类，可以对出站和入站的数据进行处理
```java
public class ClientHandler extends ChannelDuplexHandler {
    /**
     * 使用Map维护请求对象ID与响应结果Future的映射关系
     */
    private final Map<String, DefaultFuture> futureMap = new ConcurrentHashMap<>();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcResponse) {
            //获取响应对象
            RpcResponse response = (RpcResponse) msg;
            DefaultFuture defaultFuture =
            futureMap.get(response.getRequestId());
            //将结果写入DefaultFuture
            defaultFuture.setResponse(response);
        }
        super.channelRead(ctx,msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RpcRequest) {
            RpcRequest request = (RpcRequest) msg;
            //发送请求对象之前，先把请求ID保存下来，并构建一个与响应Future的映射关系
            futureMap.putIfAbsent(request.getRequestId(), new DefaultFuture());
        }
        super.write(ctx, msg, promise);
    }

    /**
     * 获取响应结果
     *
     * @param requsetId
     * @return
     */
    public RpcResponse getRpcResponse(String requsetId) {
        try {
            DefaultFuture future = futureMap.get(requsetId);
            return future.getRpcResponse(5000);
        } finally {
            //获取成功以后，从map中移除
            futureMap.remove(requsetId);
        }
    }
}

```
> 参考文章： https://xilidou.com/2018/09/26/dourpc-remoting/


从上面实现可以看出，我们定义了一个Map,维护请求ID与响应结果的映射关系，目的是为了客户端用来验证服务端响应是否与请求相匹配，因为Netty的channel可能被多个线程使用，当结果返回时，你不知道是从哪个线程返回的，所以需要一个映射关系。

而我们的结果是封装在DefaultFuture中的，因为Netty是异步框架，所有的返回都是基于Future和Callback机制的，我们这里自定义Future来实现客户端"异步调用"
```java
public class DefaultFuture {
    private RpcResponse rpcResponse;
    private volatile boolean isSucceed = false;
    private final Object object = new Object();

    public RpcResponse getRpcResponse(int timeout) {
        synchronized (object) {
            while (!isSucceed) {
                try {
                    object.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return rpcResponse;
        }
    }

    public void setResponse(RpcResponse response) {
        if (isSucceed) {
            return;
        }
        synchronized (object) {
            this.rpcResponse = response;
            this.isSucceed = true;
            object.notify();
        }
    }
}

```
- 实际上用了wait和notify机制，同时使用一个boolean变量做辅助


### 5. Netty服务端

Netty服务端的实现跟客户端的实现差不多，只不过要注意的是，当对请求进行解码过后，需要通过代理的方式调用本地函数。下面是Server端代码：
```java
public class NettyServer implements InitializingBean {
    private EventLoopGroup boss = null;
    private EventLoopGroup worker = null;
    @Autowired
    private ServerHandler serverHandler;
    @Override
    public void afterPropertiesSet() throws Exception {
        //此处使用了zookeeper做注册中心，本文不涉及，可忽略
        ServiceRegistry registry = new ZkServiceRegistry("127.0.0.1:2181");
        start(registry);
    }

    public void start(ServiceRegistry registry) throws Exception {
        //负责处理客户端连接的线程池
        boss = new NioEventLoopGroup();
        //负责处理读写操作的线程池
        worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //添加解码器
                        pipeline.addLast(new RpcEncoder(RpcResponse.class, new JSONSerializer()));
                        //添加编码器
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONSerializer()));
                        //添加请求处理器
                        pipeline.addLast(serverHandler);

                    }
                });
        bind(serverBootstrap, 8888);
    }

    /**
     * 如果端口绑定失败，端口数+1,重新绑定
     *
     * @param serverBootstrap
     * @param port
     */
    public void bind(final ServerBootstrap serverBootstrap,int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("端口[ {} ] 绑定成功",port);
            } else {
                log.error("端口[ {} ] 绑定失败", port);
                bind(serverBootstrap, port + 1);
            }
        });
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        log.info("关闭Netty");
    }
}
```
下面是处理读写操作的Handler类：
```java
@Component
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(msg.getRequestId());
        try {
            Object handler = handler(msg);
            log.info("获取返回结果: {} ", handler);
            rpcResponse.setResult(handler);
        } catch (Throwable throwable) {
            rpcResponse.setError(throwable.toString());
            throwable.printStackTrace();
        }
        ctx.writeAndFlush(rpcResponse);
    }

    /**
     * 服务端使用代理处理请求
     *
     * @param request
     * @return
     */
    private Object handler(RpcRequest request) throws ClassNotFoundException, InvocationTargetException {
        //使用Class.forName进行加载Class文件
        Class<?> clazz = Class.forName(request.getClassName());
        Object serviceBean = applicationContext.getBean(clazz);
        log.info("serviceBean: {}",serviceBean);
        Class<?> serviceClass = serviceBean.getClass();
        log.info("serverClass:{}",serviceClass);
        String methodName = request.getMethodName();

        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //使用CGLIB Reflect
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
        log.info("开始调用CGLIB动态代理执行服务端方法...");
        return fastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```


### 6. 客户端代理
客户端使用Java动态代理，在代理类中实现通信细节，众所众知，Java动态代理需要实现InvocationHandler接口
```java
@Slf4j
public class RpcClientDynamicProxy<T> implements InvocationHandler {
    private Class<T> clazz;
    public RpcClientDynamicProxy(Class<T> clazz) throws Exception {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        String requestId = UUID.randomUUID().toString();

        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Class<?>[] parameterTypes = method.getParameterTypes();

        request.setRequestId(requestId);
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(args);
        log.info("请求内容: {}",request);
        
        //开启Netty 客户端，直连
        NettyClient nettyClient = new NettyClient("127.0.0.1", 8888);
        log.info("开始连接服务端：{}",new Date());
        nettyClient.connect();
        RpcResponse send = nettyClient.send(request);
        log.info("请求调用返回结果：{}", send.getResult());
        return send.getResult();
    }
}

```
- 在invoke方法中封装请求对象，构建NettyClient对象，并开启客户端，发送请求消息

代理工厂类如下：
```java
public class ProxyFactory {
    public static <T> T create(Class<T> interfaceClass) throws Exception {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class<?>[] {interfaceClass}, new RpcClientDynamicProxy<T>(interfaceClass));
    }
}
```
- 通过Proxy.newProxyInstance创建接口的代理类

### 7. RPC远程调用测试
API：
```java
public interface HelloService {
    String hello(String name);
}
```
- 准备一个测试API接口


客户端：
```java
@SpringBootApplication
@Slf4j
public class ClientApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ClientApplication.class, args);
        HelloService helloService = ProxyFactory.create(HelloService.class);
        log.info("响应结果“: {}",helloService.hello("pjmike"));
    }
}
```
- 客户端调用接口的方法


服务端：
```java
//服务端实现
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello, " + name;
    }
}
```

运行结果：

![rpc_5](https://pjmike-1253796536.cos.ap-beijing.myqcloud.com/%E5%88%86%E5%B8%83%E5%BC%8F/RPC/rpc_58_5.png)



## 小结

以上我们基于Netty实现了一个非非非常简陋的RPC框架，比起成熟的RPC框架来说相差甚远，甚至说基本的注册中心都没有实现，但是通过本次实践，可以说我对于RPC的理解更深了，了解了一个RPC框架到底需要关注哪些方面，未来当我们使用成熟的RPC框架时，比如Dubbo，能够做到心中有数，能明白其底层不过也是使用Netty作为基础通讯框架。往后，如果更深入翻看开源RPC框架源码时，也相对比较容易


## 参考资料 & 鸣谢

- https://xilidou.com/2018/09/26/dourpc-remoting/
- https://www.cnblogs.com/luxiaoxun/p/5272384.html
- https://my.oschina.net/huangyong/blog/361751
- https://www.w3cschool.cn/architectroad/architectroad-rpc-framework.html
- https://www.cnkirito.moe/dubbo27-features/
- https://juejin.im/post/5ad2a99ff265da238d51264d#heading-6
- https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b4db0e6e51d45191d79e475#heading-3
