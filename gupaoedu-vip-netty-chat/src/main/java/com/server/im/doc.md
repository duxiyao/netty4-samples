# 中间件责任
- relay netty实现，udp服务，进行音视频及消息的转发，设置发送应答机制
- zookeeper 用于各服务间的发现，亦使relay支持分布式
- kafka 订阅发布、消息队列、日志采集
- redis 存储信息
- dubbo 各relay间进行中转调用
- spring cloud微服务做业务

# 数据交换协议
[fromid(36B utf-8)][toid(36B utf-8)][pkgid(36B utf-8)][type(1B)][version(1B)]
[pkgcnt(1B)][cpkgn(1B)]
[datalen(2B)][data]  {data中结构[timestamp8B][jsonlen(2B)][json(utf-8)][字节流]}
fromid 36个字节 utf-8
toid 36个字节 utf-8
pkgid，包id 36个字节 utf-8
type,1字节数据类型 
version,1字节版本号 
pkgcnt,N个包为完整 
cpkgn,当前第n个包 
datalen，2字节数据长度，不包含之前的不包含自身，包含之后的

data,剩余字节流数据，若是媒体数据，需要由时间戳表示媒体包的顺序.timestamp为long的时间戳
jsonlen, 2字节json长度 json数据 字节流数据

后续可将json换成protobuf

~~、[from(1B)][fromid][to(1B)][toid][type(1B)][version(1B)]~~
~~[pkgidlen(1B)][pkgid(utf-8)][pkgcnt(1B)][cpkgn(1B)]~~
~~[datalen(2B)][data]{data中结构[timestamp8B][jsonlen(2B)][json(utf-8)][字节流]}~~
~~fromid~~
~~toid~~
~~type,1字节数据类型 ~~
~~version,1字节版本号 ~~
~~pkgid，包id ~~
~~pkgcnt,N个包为完整 ~~
~~cpkgn,当前第n个包 ~~
~~datalen，2字节数据长度，不包含之前的不包含自身，包含之后的~~

~~data,剩余字节流数据，若是媒体数据，需要由时间戳表示媒体包的顺序.timestamp为long的时间戳~~
~~jsonlen, 2字节json长度 json数据 字节流数据~~

## 消息
消息通过relay转发、后续可加上stun进行nat打洞。若A发消息给B，而A、B不在同一relay上，则通过redis获取对应的relay,并通过dubbo中继，过程中若发现不在线，
则要有回馈,可利用kafka订阅发布。同样，终端与relay与终端间数据传递，也要有应答机制，主要用于索取丢失的包。
【普通消息(tcp ? udp ?) 音视频消息 udp】
## 索取丢失的数据包
type=1 ，从内存中获取，服务器缓存30秒。 若是分布式，则从redis中获取
## 普通消息  [type(1B)]=11
[data] 为utf-8编码的字符串
## 文件消息
## 媒体数据
### 音频数据
### 视频数据


# redis数据结构
- 终端信息，包含注册的服务器信息及个人信息 hash map
  > - key：终端的id  
  >>                                                      >
  > - key：终端的id_connect_info  (利用过期时间，结合心跳包维护是否在线状态)  
  >> ip:port                                                
  >> serverIp:serverPort                                                