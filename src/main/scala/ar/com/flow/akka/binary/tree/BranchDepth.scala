package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Depth
import ar.com.flow.akka.binary.tree.BinaryTreeDepth._
import ar.com.flow.akka.binary.tree.BranchDepth.{LeftDepth, RightDepth}

object BranchDepth {
  final case class LeftDepth(replyTo: ActorRef[BinaryTree.Command],
                             leftChild: Option[ActorRef[BinaryTree.Command]] = None) extends BinaryTree.Command


  final case class RightDepth(replyTo: ActorRef[BinaryTree.Command],
                              rightChild: Option[ActorRef[BinaryTree.Command]] = None) extends BinaryTree.Command

  def apply(): Behavior[BinaryTree.Command] =
    Behaviors.setup(context => new BranchDepth(context))
}

class BranchDepth(context: ActorContext[BinaryTree.Command]) extends AbstractBehavior[BinaryTree.Command](context) {
  override def onMessage(message: BinaryTree.Command): Behavior[BinaryTree.Command] = {
    message match {
      case LeftDepth(replyTo, None) =>
        replyTo ! ReturnedLeftDepth(0)
        this
      case LeftDepth(replyTo, Some(leftChild)) =>
        leftChild ! Depth(replyTo)
        this
      case RightDepth(replyTo, None) =>
        replyTo ! ReturnedRightDepth(0)
        this
      case RightDepth(replyTo, Some(rightChild)) =>
        rightChild ! Depth(replyTo)
        this
    }
  }
}
