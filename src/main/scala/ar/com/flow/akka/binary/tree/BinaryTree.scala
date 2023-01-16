package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object BinaryTree {
  sealed trait Command

  final case class Path(replyTo: ActorRef[PathReturned], collectedPath: Option[String] = None) extends Command
  final case class PathReturned(path: String) extends Command

  final case class Depth(replyTo: ActorRef[DepthReturned]) extends Command
  final case class DepthReturned(depth: Int) extends Command

  final case class Parent(replyTo: ActorRef[NodeReturned]) extends Command

  final case class AddLeftChild(replyTo: ActorRef[NodeReturned], value: Int, leftChild: Option[NodeState], rightChild: Option[NodeState]) extends Command
  final case class LeftChild(replyTo: ActorRef[NodeReturned]) extends Command
  final case class AddRightChild(replyTo: ActorRef[NodeReturned], value: Int, leftChild: Option[NodeState], rightChild: Option[NodeState]) extends Command
  final case class RightChild(replyTo: ActorRef[NodeReturned]) extends Command
  final case class NodeState(value: Int, leftChild: Option[NodeState], rightChild: Option[NodeState]) extends Command
  final case class NodeReturned(node: Option[ActorRef[Command]]) extends Command

  final case class Value(replyTo: ActorRef[ValueReturned]) extends Command
  final case class ValueReturned(value: Int) extends Command

  def apply(name: String = "",
            value: Int = 0,
            parent: Option[ActorRef[Command]] = None,
            leftChild: Option[NodeState] = None,
            rightChild: Option[NodeState] = None): Behavior[Command] =
    Behaviors.setup(context => new Node(context, name, value, parent, leftChild, rightChild))

  def left(value: Int = 0,
           parent: Option[ActorRef[Command]] = None,
           leftChild: Option[NodeState] = None,
           rightChild: Option[NodeState] = None): Behavior[Command] =
    Behaviors.setup(context => LeftNode(context, value, parent, leftChild, rightChild))

  def right(value: Int = 0,
           parent: Option[ActorRef[Command]] = None,
           leftChild: Option[NodeState] = None,
           rightChild: Option[NodeState] = None): Behavior[Command] =
    Behaviors.setup(context => RightNode(context, value, parent, leftChild, rightChild))
}
