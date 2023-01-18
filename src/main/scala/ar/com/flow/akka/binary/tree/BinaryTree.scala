package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTreeDepth.ReturnedDepth

object BinaryTree {
  trait Command

  final case class Parent(replyTo: ActorRef[ReturnedNode]) extends Command
  final case class AddLeftChild(replyTo: ActorRef[ReturnedNode], leftChild: Node) extends Command
  final case class LeftChild(replyTo: ActorRef[ReturnedNode]) extends Command
  final case class AddRightChild(replyTo: ActorRef[ReturnedNode], leftChild: Node) extends Command
  final case class RightChild(replyTo: ActorRef[ReturnedNode]) extends Command
  final case class ReturnedNode(node: Option[ActorRef[Command]]) extends Command

  final case class Value(replyTo: ActorRef[ReturnedValue]) extends Command
  final case class ReturnedValue(value: Int) extends Command

  final case class Path(replyTo: ActorRef[ReturnedPath], collectedPath: Option[String] = None) extends Command
  final case class ReturnedPath(path: String) extends Command

  final case class Depth(replyTo: ActorRef[ReturnedDepth]) extends Command

  final case class Node(value: Int, leftChild: Option[Node] = None, rightChild: Option[Node] = None)

  object Leaf {
    def apply(value: Int): Node = Node(value)
  }

  def apply(name: String = "",
            value: Int = 0,
            parent: Option[ActorRef[Command]] = None,
            leftChild: Option[Node] = None,
            rightChild: Option[Node] = None): Behavior[Command] =
    Behaviors.setup(context => new NodeBehavior(context, name, value, parent, leftChild, rightChild))

  def left(value: Int = 0,
           parent: Option[ActorRef[Command]] = None,
           leftChild: Option[Node] = None,
           rightChild: Option[Node] = None): Behavior[Command] =
    Behaviors.setup(context => LeftNode(context, value, parent, leftChild, rightChild))

  def right(value: Int = 0,
            parent: Option[ActorRef[Command]] = None,
            leftChild: Option[Node] = None,
            rightChild: Option[Node] = None): Behavior[Command] =
    Behaviors.setup(context => RightNode(context, value, parent, leftChild, rightChild))
}
