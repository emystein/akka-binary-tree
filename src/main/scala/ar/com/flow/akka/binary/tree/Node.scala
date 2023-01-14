package ar.com.flow.akka.binary.tree

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}

class Node(context: ActorContext[BinaryTree.Command],
           var value: Int = 0)
  extends AbstractBehavior[BinaryTree.Command](context) {
  import BinaryTree._

  var leftChild: Option[NodeState] = None
  var rightChild: Option[NodeState] = None

  override def onMessage(message: BinaryTree.Command): Behavior[BinaryTree.Command] = {
    message match {
      case Depth(replyTo) =>
        replyTo ! DepthReply(1)
        this
      case AddLeftChild(newValue, newLeftChild, newRightChild, replyTo) =>
        leftChild = Some(NodeState(newValue, None, None))
        replyTo ! NodeState(newValue, newLeftChild, newRightChild)
        this
      case LeftChild(replyTo) =>
        replyTo ! leftChild.getOrElse(EmptyNodeState())
        this
      case AddRightChild(newValue, newLeftChild, newRightChild, replyTo) =>
        leftChild = Some(NodeState(newValue, None, None))
        replyTo ! NodeState(newValue, newLeftChild, newRightChild)
        this
      case RightChild(replyTo) =>
        replyTo ! leftChild.getOrElse(EmptyNodeState())
        this
      case Value(replyTo) =>
        replyTo ! ValueReply(value)
        this
    }
  }
}