package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree._
import ar.com.flow.akka.binary.tree.BinaryTreePath.TreePath

object LeftNode {
  def apply(context: ActorContext[BinaryTree.Command], value: Int = 0,
            parent: Option[ActorRef[BinaryTree.Command]] = None,
            leftChildState: Option[BinaryTree.Node] = None,
            rightChildState: Option[BinaryTree.Node] = None) =
    new NodeBehavior(context, name = "left", value, parent, leftChildState, rightChildState)
}

object RightNode {
  def apply(context: ActorContext[BinaryTree.Command], value: Int = 0,
            parent: Option[ActorRef[BinaryTree.Command]] = None,
            leftChildState: Option[BinaryTree.Node] = None,
            rightChildState: Option[BinaryTree.Node] = None) =
    new NodeBehavior(context, name = "right", value, parent, leftChildState, rightChildState)
}

class NodeBehavior(context: ActorContext[BinaryTree.Command],
           val name: String = "",
           var value: Int = 0,
           var parent: Option[ActorRef[BinaryTree.Command]] = None,
           leftChildState: Option[BinaryTree.Node] = None,
           rightChildState: Option[BinaryTree.Node] = None
          )
  extends AbstractBehavior[BinaryTree.Command](context) {

  var leftChild: Option[ActorRef[Command]] = leftChildState.map(s => spawnLeftChild(s.value, s.leftChild, s.rightChild))
  var rightChild: Option[ActorRef[Command]] = rightChildState.map(s => spawnRightChild(s.value, s.leftChild, s.rightChild))

  override def onMessage(message: BinaryTree.Command): Behavior[BinaryTree.Command] = {
    message match {
      case Path(replyTo, collectedPath) =>
        val treePath = context.spawn(BinaryTreePath(), "path")
        treePath ! TreePath(replyTo, this.parent, this.name, collectedPath)
        this
      case Depth(replyTo) =>
        replyTo ! DepthReturned(1)
        this
      case Parent(replyTo) =>
        replyTo ! NodeReturned(parent)
        this
      case AddLeftChild(replyTo, newValue, newLeftChild, newRightChild) =>
        leftChild = Some(spawnLeftChild(newValue, newLeftChild, newRightChild))
        replyTo ! NodeReturned(leftChild)
        this
      case LeftChild(replyTo) =>
        replyTo ! NodeReturned(leftChild)
        this
      case AddRightChild(replyTo, newValue, newLeftChild, newRightChild) =>
        rightChild = Some(spawnRightChild(newValue, newLeftChild, newRightChild))
        replyTo ! NodeReturned(rightChild)
        this
      case RightChild(replyTo) =>
        replyTo ! NodeReturned(rightChild)
        this
      case Value(replyTo) =>
        replyTo ! ValueReturned(value)
        this
    }
  }

  private def spawnRightChild(newValue: Int, newLeftChild: Option[BinaryTree.Node], newRightChild: Option[BinaryTree.Node]) = {
    context.spawn(BinaryTree.right(newValue, parent = Some(context.self), newLeftChild, newRightChild), "right")
  }

  private def spawnLeftChild(newValue: Int, newLeftChild: Option[BinaryTree.Node], newRightChild: Option[BinaryTree.Node]) = {
    context.spawn(BinaryTree.left(newValue, parent = Some(context.self), newLeftChild, newRightChild), "left")
  }
}