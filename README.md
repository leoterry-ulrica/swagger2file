# swagger2file
支持swagger2-3.0.0版本导出文件，目前优先考虑导出word。

# 编译，启动
```shell script
>mvn clean package -DskipTests;
>java -jar swagger2file-1.0.0-SNAPSHOT.jar
```

# 访问地址
[http://127.0.0.1:8899/doc.html](http://127.0.0.1:8899/doc.html)

# 接口调用
导出文档：[http://127.0.0.1:8899/s2f/exp/doc?swaggerUrl=${替换成swagger2-3.0.0版本的API元数据JSON数据地址}](http://127.0.0.1:8899/s2f/exp/doc?swaggerUrl=${替换成swagger2-3.0.0版本的API元数据JSON数据地址})
