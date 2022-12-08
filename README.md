# java-agent-demo

尝试模仿SkyWalking实现一个简版的APM

## Quick Start
1. 项目下包含两个子模块，`simple-agent`是我们的探针应用（Java Agent），`simple-app`是一个简单的SpringBoot应用，用来测试我们的探针应用
2. 执行`simple-agent`项目下的`build.sh`，执行成功后会在当前目录下生成`simple-agent.tar.gz`
```shell
cd simple-agent && bash build.sh
```
3. 将得到的压缩包拷贝并解压至「目标应用程序」所在的服务器，修改`agent/config/agent.config`配置文件
4. 启动「目标应用程序」时添加javaagent参数
```shell
java -javaagent:/path/to/agent/simple-agent.jar -jar simple-app.jar
```


## Feature List

## Todo List
- Tracing日志
- 在调用链路中透传自定义标记，例如透传「压测标」
- 收集JVM指标数据

## SkyWalking部分源码解析
- [SkyWalking Agent启动流程详解](docs/Agent-Startup.md)
- [SkyWalking中的日志框架详解](docs/Logging-Module.md)
- [SkyWalking中用到的maven插件详解](docs/Maven-Plugins-Used.md)
