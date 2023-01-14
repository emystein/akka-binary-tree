package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object Node {
  sealed trait Command

  final case class Depth(replyTo: ActorRef[DepthReply]) extends Command
  final case class DepthReply(depth: Int) extends Command

  final case class LeftChild(replyTo: ActorRef[NodeReply]) extends Command
  final case class RightChild(replyTo: ActorRef[NodeReply]) extends Command

  final case class NodeReply(node: Option[Node]) extends Command

  final case class Value(replyTo: ActorRef[ValueReply]) extends Command
  final case class ValueReply(value: Int) extends Command

  def apply(value: Int = 0): Behavior[Command] = Behaviors.setup(context => new Node(context, value))
}

class Node(context: ActorContext[Node.Command],
           var value: Int = 0)
  extends AbstractBehavior[Node.Command](context) {
  import Node._

  var leftChild: Option[Node] = None
  var rightChild: Option[Node] = None

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Depth(replyTo) =>
        replyTo ! DepthReply(1)
        this
      case LeftChild(replyTo) =>
        replyTo ! NodeReply(leftChild)
        this
      case RightChild(replyTo) =>
        replyTo ! NodeReply(rightChild)
        this
      case Value(replyTo) =>
        replyTo ! ValueReply(value)
        this
    }
  }
}