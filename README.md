# java-agent-demo

尝试自己实现一个简单的APM系统

## QuickStart
使用mvn命令生成对应的jar包，然后启动目标应用程序时添加jvm参数: -javaagent demo-agent.jar
```shell
 cd simple-agent && mvn package -Dcheckstyle.skip -DskipTests
```
