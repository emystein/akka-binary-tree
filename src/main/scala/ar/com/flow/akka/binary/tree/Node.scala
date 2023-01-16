package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree._
import ar.com.flow.akka.binary.tree.BinaryTreePath.TreePath

object LeftNode {
  def apply(context: ActorContext[BinaryTree.Command], value: Int = 0,
            parent: Option[ActorRef[BinaryTree.Command]] = None,
            leftChild: Option[BinaryTree.Node] = None,
            rightChild: Option[BinaryTree.Node] = None) =
    new NodeBehavior(context, name = "left", value, parent, leftChild, rightChild)
}

object RightNode {
  def apply(context: ActorContext[BinaryTree.Command], value: Int = 0,
            parent: Option[ActorRef[BinaryTree.Command]] = None,
            leftChild: Option[BinaryTree.Node] = None,
            rightChild: Option[BinaryTree.Node] = None) =
    new NodeBehavior(context, name = "right", value, parent, leftChild, rightChild)
}

class NodeBehavior(context: ActorContext[BinaryTree.Command],
                   val name: String = "",
                   var value: Int = 0,
                   val parent: Option[ActorRef[BinaryTree.Command]] = None,
                   initialLeftChild: Option[BinaryTree.Node] = None,
                   initialRightChild: Option[BinaryTree.Node] = None)
  extends AbstractBehavior[BinaryTree.Command](context) {

  var leftChild: Option[ActorRef[Command]] = initialLeftChild.map(spawnLeftChild)
  var rightChild: Option[ActorRef[Command]] = initialRightChild.map(spawnRightChild)

  override def onMessage(message: BinaryTree.Command): Behavior[BinaryTree.Command] = {
    message match {
      case Path(replyTo, collectedPath) =>
        val treePath = context.spawn(BinaryTreePath(), "path")
        treePath ! TreePath(replyTo, this.parent, this.name, collectedPath)
        this
      case Depth(replyTo) =>
        replyTo ! ReturnedDepth(1)
        this
      case Parent(replyTo) =>
        replyTo ! ReturnedNode(parent)
        this
      case AddLeftChild(replyTo, newLeftChild) =>
        leftChild = Some(spawnLeftChild(newLeftChild))
        replyTo ! ReturnedNode(leftChild)
        this
      case LeftChild(replyTo) =>
        replyTo ! ReturnedNode(leftChild)
        this
      case AddRightChild(replyTo, newRightChild) =>
        rightChild = Some(spawnRightChild(newRightChild))
        replyTo ! ReturnedNode(rightChild)
        this
      case RightChild(replyTo) =>
        replyTo ! ReturnedNode(rightChild)
        this
      case Value(replyTo) =>
        replyTo ! ReturnedValue(value)
        this
    }
  }

  private def spawnLeftChild(leftChild: BinaryTree.Node) = {
    context.spawn(BinaryTree.left(leftChild.value, parent = Some(context.self), leftChild.leftChild, leftChild.rightChild), "left")
  }

  private def spawnRightChild(rightChild: BinaryTree.Node) = {
    context.spawn(BinaryTree.right(rightChild.value, parent = Some(context.self), rightChild.leftChild, rightChild.rightChild), "right")
  }
}