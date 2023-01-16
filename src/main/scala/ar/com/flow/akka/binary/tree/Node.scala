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

  var leftChild: Option[ActorRef[Command]] = initialLeftChild.map(s => spawnLeftChild(s.value, s.leftChild, s.rightChild))
  var rightChild: Option[ActorRef[Command]] = initialRightChild.map(s => spawnRightChild(s.value, s.leftChild, s.rightChild))

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
      case AddLeftChild(replyTo, newValue, newLeftChild, newRightChild) =>
        leftChild = Some(spawnLeftChild(newValue, newLeftChild, newRightChild))
        replyTo ! ReturnedNode(leftChild)
        this
      case LeftChild(replyTo) =>
        replyTo ! ReturnedNode(leftChild)
        this
      case AddRightChild(replyTo, newValue, newLeftChild, newRightChild) =>
        rightChild = Some(spawnRightChild(newValue, newLeftChild, newRightChild))
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

  private def spawnRightChild(newValue: Int, newLeftChild: Option[BinaryTree.Node], newRightChild: Option[BinaryTree.Node]) = {
    context.spawn(BinaryTree.right(newValue, parent = Some(context.self), newLeftChild, newRightChild), "right")
  }

  private def spawnLeftChild(newValue: Int, newLeftChild: Option[BinaryTree.Node], newRightChild: Option[BinaryTree.Node]) = {
    context.spawn(BinaryTree.left(newValue, parent = Some(context.self), newLeftChild, newRightChild), "left")
  }
}