import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool

/**
  *
  * @author sunyp
  * @version 1.0
  **/


// 定义一个case类
sealed trait SumTrait

case class Result(value: Int) extends SumTrait


// 计算用的Actor
class SumActor extends Actor {
  val RANGE = 10000

  def receive = {
    case value: Int =>
      sender ! Result(calculate((RANGE / Sum.NCPU) * (value - 1) + 1, (RANGE / Sum.NCPU) * value, value.toString))
    case _ => println("未知 in SumActor...")
  }

  def calculate(start: Int, end: Int, flag: String): Int = {
    var cal = 0

    for (i <- start to end) {
      for (j <- 1 to 3000000) {}
      cal += i
    }

    println("flag : " + flag + ".")
    cal
  }
}

// 打印结果用的Actor
class PrintActor extends Actor {
  def receive = {
    case (sum: Int, startTime: Long) =>
      println("总数为：" + sum + "；所花时间为："
        + (System.nanoTime() - startTime) / 1000000000.0 + "秒。")
    case _ => println("未知 in PrintActor...")
  }
}

// 主actor，发送计算指令给SumActor，发送打印指令给PrintActor
class MasterActor extends Actor {
  // 声明Actor实例，nrOfInstances是pool里所启routee（SumActor）的数量，
  // 这里用4个SumActor来同时计算，很Powerful。
  val sumActor = context.actorOf(Props[SumActor]
    .withRouter(RoundRobinPool(nrOfInstances = Sum.NCPU)), name = "sumActor")
  val printActor = context.actorOf(Props[PrintActor], name = "printActor")
  var sum = 0
  var count = 0
  var startTime: Long = 0

  def receive = {
    case "calculate..." =>
      startTime = System.nanoTime()
      for (i <- 1 to Sum.NCPU) sumActor ! i
    case Result(value) =>
      sum += value
      count += 1
      if (count == Sum.NCPU) {
        printActor !(sum, startTime)
        context.stop(self)
        Sum.system.shutdown()
      }
    case _ => println("未知 in MasterActor...")
  }
}

object Sum {
  val NCPU = Runtime.getRuntime.availableProcessors()
  val system = ActorSystem("MasterActorSystem")
  val masterActor = system.actorOf(Props[MasterActor], name = "masterActor")

  def main(args: Array[String]): Unit = {
    masterActor ! "calculate..."
  }
}