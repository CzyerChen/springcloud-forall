## IDE插件

1、与阿里编码规约类似，也是在插件市场中直接下载，重启IDE后生效
2、如果步骤一存在困难，可以下载官方打包的release版本，然后导入 --> https://github.com/checkstyle/checkstyle/releases?page=1。

注意checkStyle与JAVA版本的对应关系，否则启动检测你将发现提示`has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to 52.0`。

## 版本

|Checkstyle version	|JRE version|
|--|--|
|10.x	|11 and above|
|7.x, 8.x, 9.x|	8 and above|
|6.x	|6 and above|
|5.x	|5 and above|

官方文档链接：https://checkstyle.sourceforge.io/

## 流程

一般pre-commit的配置如下：
1. 在项目根目录 .git/hooks 下 找到 pre-commit.example文件，简单查阅，文档内也是会给到一定的提示。
2. `cp pre-commit.example pre-commit` 生成正式生效的文件 **pre-commit**
3. 下面主要是在这个内部书写检测逻辑，大体的一般是：
   a. 从git的提交数据中获取到修改的文件或目录，遍历文件或目录
   b. 针对每一项执行检查，具体规范具体指定，检查出修改项
   c. exitcode返回非0即不会提交代码，exitcode为0表示通过会提交代码

## 实践：CheckStyle配置git precommit check

指令：`java -jar ./checkstyle-8.45-all.jar  -c ./checkStyle.xml /Task.java -f text -o outputTempFile`

-jar 指定checkstyle jar包位置
-c   指定检查的配置文件 后面紧跟要检查的文件或目录
-f   指定输出的格式类型 有XML, SARIF, PLAIN
-o   指定输出检查结果的位置，默认不指定是输出到终端

样例：

```shell
#!/bin/sh
# claire

# From java package
# checkStyle version: 8.45
# jdk version: 1.8

function print(){
echo "checkStyle>> $*"
}

print "Javacode stylecheck starting, please wait..."
wd=`pwd`
print "Workdir: $wd"

check_jar_path="$wd/.git/jars/checkstyle-8.45-all.jar"
check_xml_path="$wd/.git/files/checkStyle.xml"
check_result_file="$wd/temp"

# 清空temp文件
rm -rf $check_result_file

is_err=0
is_warn=0

path=''
for file in `git status --porcelain | sed s/^...// | grep '\.java$' | grep -v 'test'`;
do
path+="$wd/$file "
done
if [ "x${path}" != "x" ];then
  print "Check file: $path"
  re=`java -jar $check_jar_path -c $check_xml_path $path -f plain -o $check_result_file`
  err=`cat temp | grep "ERROR"`
  err_count=`cat temp | grep "ERROR" | wc -l`
  warn=`cat temp | grep "WARN"`
  warn_count=`cat temp | grep "WARN" | wc -l`
  info=`cat temp`
  if [[ $err = *"ERROR"* ]];then
    print "detect error lines count: ${err_count}"
    print "${err}"
    is_err=1
  fi
  if [[ $warn = *"WARN"* ]];then
    print "detect warning lines count: ${warn_count}"
    print "${warn}"
    is_warn=1
  fi
fi
print "Javacode stylecheck finished. Thank you for your commit!"

rm -rf $check_result_file

if [ $is_err -ne 0 ] || [ $is_warn -ne 0 ]
then
print "Please return and fix stylecheck warnings before code commit！"
exit 1
fi

exit 0
```

- git add .
- git commit -m "test"

提交后就会开启代码检测，如果存在拦截的逻辑，就无法继续commit

```bash
 git commit -m "test2"
checkStyle>> Javacode stylecheck starting, please wait...
checkStyle>> Workdir: XXX
checkStyle>> Check file: xxx/Task.java 
Checkstyle ends with 28 errors.
checkStyle>> detect error lines count:       28
checkStyle>> [ERROR] xxx/Task.java:3: Not allow chinese character ! [RegexpSingleline]
......
[ERROR] xxx/Task.java:71:29: '+' is not preceded with whitespace. [WhitespaceAround]
checkStyle>> Javacode stylecheck finished. Thank you for your commit!
checkStyle>> Please return and fix stylecheck warnings before code commit！
```