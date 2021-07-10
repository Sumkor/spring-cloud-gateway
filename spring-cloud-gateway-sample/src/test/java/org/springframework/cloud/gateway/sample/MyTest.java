package org.springframework.cloud.gateway.sample;

/**
 * @author Sumkor
 * @since 2021/7/4
 */
public class MyTest {

	/**
	 * 1. 项目启动
	 *
	 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
	 * @see org.springframework.context.support.AbstractApplicationContext#finishBeanFactoryInitialization(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#preInstantiateSingletons()
	 *
	 * 2. 创建 myRoutes Bean
	 *
	 * @see org.springframework.beans.factory.support.AbstractBeanFactory#getBean(java.lang.String)
	 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])
	 *
	 * 通过反射来初始化 myRoutes 实例，调用用户代码
	 * @see org.springframework.cloud.gateway.sample.GatewaySampleApplication#myRoutes(org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder)
	 * @see org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder#route(java.util.function.Function)
	 *
	 * 设置 path 值作为 pattern，传递给 RoutePredicateFactory
	 * @see org.springframework.cloud.gateway.route.builder.PredicateSpec#path(java.lang.String...)
	 * @see org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory#applyAsync(java.util.function.Consumer)
	 * @see org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory#apply(org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory.Config)
	 *
	 * 设置 filter，传递给 AddRequestHeaderGatewayFilterFactory
	 * @see org.springframework.cloud.gateway.route.builder.BooleanSpec#filters(java.util.function.Function)
	 * @see org.springframework.cloud.gateway.route.builder.GatewayFilterSpec#addRequestHeader(java.lang.String, java.lang.String)
	 * @see org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory#apply(org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory.NameValueConfig)
	 *
	 * 设置 uri
	 * @see org.springframework.cloud.gateway.route.builder.UriSpec#uri(java.lang.String)
	 *
	 * 最后，完成用户自定义 route 的构建
	 * @see org.springframework.cloud.gateway.route.Route.AbstractBuilder#build()
	 */

	/**
	 * 请求流程
	 *
	 * 1. 从 netty 中接收到请求，传递给 reactor 的 Mono 对象
	 * @see reactor.core.publisher.Mono#subscribe(org.reactivestreams.Subscriber)
	 *
	 * 2. 传递到 Spring WebFlux
	 * @see org.springframework.web.reactive.DispatcherHandler
	 * @see org.springframework.web.reactive.handler.AbstractHandlerMapping#getHandler(org.springframework.web.server.ServerWebExchange)
	 *
	 * 3. 传递到 Spring gateway 的 handler
	 * @see org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping#getHandlerInternal(org.springframework.web.server.ServerWebExchange)
	 * @see org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping#lookupRoute(org.springframework.web.server.ServerWebExchange)
	 * 其中，routeLocator.getRoutes() 得到了在 {@link GatewaySampleApplication#myRoutes} 中配置的路由规则对象。
	 *
	 * 3.1 执行路由规则匹配
	 * 进入
	 * @see org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory#apply(org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory.Config)
	 * 执行其中的 GatewayPredicate#test 方法。
	 * 这里判断 path 为 "/get"，符合该过滤器规则。
	 *
	 * 3.2 执行过滤器链
	 * @see org.springframework.cloud.gateway.handler.FilteringWebHandler#handle(org.springframework.web.server.ServerWebExchange)
	 * @see org.springframework.cloud.gateway.handler.FilteringWebHandler.DefaultGatewayFilterChain#filter(org.springframework.web.server.ServerWebExchange)
	 * 按 order 遍历所有的过滤器，当执行用户配置的过滤器，进入
	 * @see org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory#apply(org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory.NameValueConfig)
	 * 执行其中的 GatewayFilter#filter 方法。
	 * 这里为请求头添加指定信息。
	 */

	/**
	 * 访问：http://localhost:8080/get
	 * 返回：http://httpbin.org:80 地址的 JSON 数据
	 * 日志内容如下：
	 *
	 * [ctor-http-nio-6] r.n.http.server.HttpServerOperations     : [id:093f7fdb, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] New http connection, requesting read
	 * [ctor-http-nio-6] reactor.netty.transport.TransportConfig  : [id:093f7fdb, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] Initialized pipeline DefaultChannelPipeline{(reactor.left.httpCodec = io.netty.handler.codec.http.HttpServerCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.server.HttpTrafficHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
	 * [ctor-http-nio-6] r.n.http.server.HttpServerOperations     : [id:093f7fdb, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] Increasing pending responses, now 1
	 * [ctor-http-nio-6] reactor.netty.http.server.HttpServer     : [id:093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] Handler is being applied: org.springframework.http.server.reactive.ReactorHttpHandlerAdapter@1343f67a
	 * [ctor-http-nio-6] o.s.c.g.f.WeightCalculatorWebFilter      : Weights attr: {}
	 * [ctor-http-nio-6] o.s.c.g.h.p.PathRoutePredicateFactory    : Pattern "/get" matches against value "/get"
	 * [ctor-http-nio-6] o.s.c.g.h.RoutePredicateHandlerMapping   : Route matched: 204c0f1f-c8aa-491a-beed-2e732e09d1a8
	 * [ctor-http-nio-6] o.s.c.g.h.RoutePredicateHandlerMapping   : Mapping [Exchange: GET http://localhost:8080/get] to Route{id='204c0f1f-c8aa-491a-beed-2e732e09d1a8', uri=http://httpbin.org:80, order=0, predicate=Paths: [/get], match trailing slash: true, gatewayFilters=[[[AddRequestHeader Hello = 'World!!!'], order = 0]], metadata={}}
	 * [ctor-http-nio-6] o.s.c.g.h.RoutePredicateHandlerMapping   : [093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] Mapped to org.springframework.cloud.gateway.handler.FilteringWebHandler@39394ee9
	 * [ctor-http-nio-6] o.s.c.g.handler.FilteringWebHandler      : Sorted gatewayFilterFactories: [[GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.RemoveCachedBodyFilter@58f437b0}, order = -2147483648], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter@74fab04a}, order = -2147482648], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.NettyWriteResponseFilter@3bd6ba24}, order = -1], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.ForwardPathFilter@4c7e978c}, order = 0], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.GatewayMetricsFilter@22ebccb9}, order = 0], [[AddRequestHeader Hello = 'World!!!'], order = 0], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter@20f6f88c}, order = 10000], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.config.GatewayNoLoadBalancerClientAutoConfiguration$NoLoadBalancerClientFilter@5875de6a}, order = 10150], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.WebsocketRoutingFilter@354e7004}, order = 2147483646], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.NettyRoutingFilter@26c89563}, order = 2147483647], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.ForwardRoutingFilter@4277127c}, order = 2147483647]]
	 * [ctor-http-nio-6] o.s.c.g.filter.RouteToRequestUrlFilter   : RouteToRequestUrlFilter start
	 * [ctor-http-nio-6] r.n.resources.PooledConnectionProvider   : [id:b77fc1e1] Created a new pooled channel, now: 0 active connections, 0 inactive connections and 0 pending acquire requests.
	 * [ctor-http-nio-6] reactor.netty.transport.TransportConfig  : [id:b77fc1e1] Initialized pipeline DefaultChannelPipeline{(reactor.left.httpCodec = io.netty.handler.codec.http.HttpClientCodec), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
	 * [ctor-http-nio-6] r.netty.transport.TransportConnector     : [id:b77fc1e1] Connecting to [httpbin.org/18.235.124.214:80].
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] Registering pool release on close event for channel
	 * [ctor-http-nio-6] r.n.resources.PooledConnectionProvider   : [id:b77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] Channel connected, now: 1 active connections, 0 inactive connections and 0 pending acquire requests.
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] onStateChange(PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80]}, [connected])
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80]}}, [configured])
	 * [ctor-http-nio-6] r.netty.http.client.HttpClientConnect    : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] Handler is being applied: {uri=http://httpbin.org/get, method=GET}
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80]}}, [request_prepared])
	 * [ctor-http-nio-6] o.s.c.gateway.filter.NettyRoutingFilter  : outbound route: b77fc1e1, inbound: [093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988]
	 * [ctor-http-nio-6] reactor.netty.channel.FluxReceive        : [id:093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] FluxReceive{pending=0, cancelled=false, inboundDone=true, inboundError=null}: subscribing inbound receiver
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80]}}, [request_sent])
	 * [ctor-http-nio-6] r.n.http.client.HttpClientOperations     : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] Received response (auto-read:false) : [Date=Sun, 04 Jul 2021 08:46:46 GMT, Content-Type=application/json, Connection=keep-alive, Server=gunicorn/19.9.0, Access-Control-Allow-Origin=*, Access-Control-Allow-Credentials=true, content-length=787]
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80]}}, [response_received])
	 * [ctor-http-nio-6] o.s.c.g.filter.NettyWriteResponseFilter  : NettyWriteResponseFilter start inbound: b77fc1e1, outbound: [093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988]
	 * [ctor-http-nio-6] reactor.netty.channel.FluxReceive        : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] FluxReceive{pending=0, cancelled=false, inboundDone=false, inboundError=null}: subscribing inbound receiver
	 * [ctor-http-nio-6] r.n.http.client.HttpClientOperations     : [id:b77fc1e1-1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] Received last HTTP packet
	 * [ctor-http-nio-6] o.s.c.g.filter.GatewayMetricsFilter      : spring.cloud.gateway.requests tags: [tag(httpMethod=GET),tag(httpStatusCode=200),tag(outcome=SUCCESSFUL),tag(routeId=204c0f1f-c8aa-491a-beed-2e732e09d1a8),tag(routeUri=http://httpbin.org:80),tag(status=OK)]
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80]}}, [response_completed])
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80]}}, [disconnecting])
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] Releasing channel
	 * [ctor-http-nio-6] r.n.resources.PooledConnectionProvider   : [id:b77fc1e1, L:/192.168.1.107:52989 - R:httpbin.org/18.235.124.214:80] Channel cleaned, now: 0 active connections, 1 inactive connections and 0 pending acquire requests.
	 * [ctor-http-nio-6] r.n.http.server.HttpServerOperations     : [id:093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] Last HTTP response frame
	 * [ctor-http-nio-6] r.n.http.server.HttpServerOperations     : [id:093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] Decreasing pending responses, now 0
	 * [ctor-http-nio-6] r.n.http.server.HttpServerOperations     : [id:093f7fdb-1, L:/127.0.0.1:8080 - R:/127.0.0.1:52988] Last HTTP packet was sent, terminating the channel
	 * [ctor-http-nio-6] r.n.r.DefaultPooledConnectionProvider    : [id:b77fc1e1, L:/192.168.1.107:52989 ! R:httpbin.org/18.235.124.214:80] onStateChange(PooledConnection{channel=[id: 0xb77fc1e1, L:/192.168.1.107:52989 ! R:httpbin.org/18.235.124.214:80]}, [disconnecting])
	 */
}
