##Kafka错误重现
>不能重现的bug不叫bug

所以为了查找数据流当中出现的问题，我们可以从源头开始重现bug

一般的bug有：

* 程序bug，因为写的时候逻辑有问题导致bug
* 服务器有bug，在运行时因为某个服务器挂掉导致一部分数据丢失，或则硬盘损坏，空间不足等

如何得知出bug了：

* 监视每一个流程的输入/输出
* 分析每个结果的合理性

为了查找bug发生在哪个环节，我们必须有一个标准数据源，我们（假装）确信这个数据源的数据一定是正确的。一切的排查都可以从这个源头出发。

因此我的想法是，当我们发现某个时间段或则某个用户或则其他特定条件的信息流出现问题的时候，我们从数据库里面读出这一部分数据，然后重新发送到kafka数据流监视结果。我做了一个小demo，在这个demo里面，我们假设数据的格式是user text, time bigint, log text存在Cassandra中，现在我们发现某一个特定user的数据有异常，所以我们需要从Cassandra中读出这个用户的数据发送到Kafka中。

为了把这部分数据与正常数据流分开，我们在发送的json数据格式中附加一个“mode”为DEBUG。对于DEBUG mode的数据，下游会将自己的名字放在"path"里面发送到一个专门的kafka topic里面。这样我们就可以通过分析这个debug topic里面收到的信息来确定数据在哪个环节出了问题。

比如在这个demo里面，dataflow有两级:

Kafka source---->SamzaStream5---->SamzaStream10

这里用Samza，但是理论上说任何兼容Kafka的都可以。SamzaStream5和SamzaStream10是两个Samza Job，SamzaStream5是判断log是不是一个大于5的数字，如果是的话，发送到另一个Kafka topic g5，SamzaStream10从g5接收消息，然后判断log是不是一个大于10的数字，如果是的话发送到另一个topic g10。其实是一个简单的多级过滤器。

log的格式是String,但是在生成的时候使用的是随机整数，所以理论上说可以parse成整数，但是有些log多带了一个空格，如果不trim的话在parse的时候就会抛出异常，从而导致数据流在这里出错。在SamzaStream5中有trim，但是SamzaStream10里面没有trim。

下面这个图就是debug的输出，其中"16 "（带空格）只出现在"A"(SamzaStream5)这一级，而并没有出现在“B”(SamzaStream10)这里，因此我们就知道在A和B中间出现了问题。

![Kafka Output](https://raw.githubusercontent.com/foolchi/kafka-error-reproduce/master/pic/reproduce.png)

###优点
* debug过程是一个自动化的过程，我们可以选择性的重现符合某一个特定条件的数据
* 几乎可以重现所有bug，比如程序bug，比如cluster某一个特定模块bug
* 可以自动化测试，比如我们的数据库里面可以存放一部分附带各种边界条件的数据，在每个新版本发布前，可以重新跑一遍数据，比较结果与正确结果的差别（这比较的过程也可以很方便的通过程序自动化）
* 因为只有在debug的时候才会附加"mode"，所以对正常模式数据无影响，只是会多了一个判断是否存在“mode”的过程，因此对性能无影响

###TODO
* 如何将kafka信息流可视化，把所有信息流的path通过网页端绘制出来