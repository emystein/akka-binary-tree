package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object BinaryTree {
  sealed trait Command

  final case class Depth(replyTo: ActorRef[DepthReply]) extends Command
  final case class DepthReply(depth: Int) extends Command

  final case class AddLeftChild(value: Int, leftChild: Option[NodeState], rightChild: Option[NodeState]) extends Command
  final case class LeftChild(replyTo: ActorRef[NodeState]) extends Command
  final case class AddRightChild(value: Int, leftChild: Option[NodeState], rightChild: Option[NodeState]) extends Command
  final case class RightChild(replyTo: ActorRef[NodeState]) extends Command

  final case class NodeState(value: Int, leftChild: Option[NodeState], rightChild: Option[NodeState]) extends Command
  object EmptyNodeState {
    def apply(): NodeState = NodeState(value = 0, leftChild = None, rightChild = None)
  }

  final case class Value(replyTo: ActorRef[ValueReply]) extends Command
  final case class ValueReply(value: Int) extends Command

  def apply(value: Int = 0,
            leftChild: Option[NodeState] = None,
            rightChild: Option[NodeState] = None): Behavior[Command] =
    Behaviors.setup(context => new Node(context, value, leftChild, rightChild))
}
