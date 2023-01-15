package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.{ActorRef, Behavior}
import BinaryTree._

class Node(context: ActorContext[BinaryTree.Command],
           var value: Int = 0,
           var leftChildState: Option[NodeState] = None,
           var rightChildState: Option[NodeState] = None
          )
  extends AbstractBehavior[BinaryTree.Command](context) {

  var leftChild: Option[ActorRef[Command]] = None
  var rightChild: Option[ActorRef[Command]] = None

  override def onMessage(message: BinaryTree.Command): Behavior[BinaryTree.Command] = {
    message match {
      case Depth(replyTo) =>
        replyTo ! DepthReply(1)
        this
      case AddLeftChild(newValue, newLeftChild, newRightChild) =>
        leftChild = Some(context.spawn(BinaryTree(newValue, newLeftChild, newRightChild), "left"))
        leftChildState = Some(NodeState(newValue, newLeftChild, newRightChild))
        this
      case LeftChild(replyTo) =>
        replyTo ! leftChildState.getOrElse(EmptyNodeState())
        this
      case AddRightChild(newValue, newLeftChild, newRightChild) =>
        rightChild = Some(context.spawn(BinaryTree(newValue, newLeftChild, newRightChild), "right"))
        rightChildState = Some(NodeState(newValue, newLeftChild, newRightChild))
        this
      case RightChild(replyTo) =>
        replyTo ! rightChildState.getOrElse(EmptyNodeState())
        this
      case Value(replyTo) =>
        replyTo ! ValueReply(value)
        this
    }
  }
}