package com
import scala.io.Source
import akka.pattern._
import akka.actor._
import akka.actor.ActorRef
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success



class WordCount extends Actor {

    var filesender: Option[ActorRef]= None
    var child = context.actorOf(Props[Child],"child")
    var wordcount=0
    def receive={
      
      case "EOF"=>
        filesender.get ! wordcount
      case filePath:String=>
        filesender = Some(sender)
        for(line<- Source.fromFile(filePath).getLines()){
          child ! line
        }
        child ! "EOF"
      case countline :Int =>{
        wordcount=wordcount+countline
      }
    }
}
class Child extends Actor{
  
  var wordcountline=0
  def receive={
    case "EOF"=>
      sender ! "EOF"
    case line : String =>{
      wordcountline= line.split(" ").size
      sender ! wordcountline
    }
  }
}
object WordCount extends App {
 implicit val timeoutcustom = Timeout(10000)
 val system = ActorSystem("System")
 val parent = system.actorOf(Props(new WordCount()), "parent")
 val file = "abc.txt"
 val result = parent ? file
 result onComplete {
   case Success(msg) =>
     println("\n\nTotal word count " + file + s" : $msg\n\n")
     system.shutdown
   case Failure(f) =>
     println("fail: " + f)
     system.shutdown
 }
}