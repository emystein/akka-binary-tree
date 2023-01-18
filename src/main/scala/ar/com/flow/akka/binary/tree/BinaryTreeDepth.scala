package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.{Command, ReturnedDepth}
import ar.com.flow.akka.binary.tree.BinaryTreeDepth._

object BinaryTreeDepth {
  final case class Depth() extends Command

  final case class LeftDepth(replyTo: ActorRef[Command],
                             leftChild: Option[ActorRef[Command]] = None) extends Command

  final case class ReturnedLeftDepth(depth: Int = 0) extends Command

  final case class RightDepth(replyTo: ActorRef[Command],
                              rightChild: Option[ActorRef[Command]] = None) extends Command

  final case class ReturnedRightDepth(depth: Int = 0) extends Command

  def apply(replyTo: ActorRef[BinaryTree.ReturnedDepth],
            leftChild: Option[ActorRef[Command]] = None,
            rightChild: Option[ActorRef[Command]] = None): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreeDepth(context, replyTo, leftChild, rightChild))
}

class BinaryTreeDepth(context: ActorContext[Command],
                      replyTo: ActorRef[BinaryTree.ReturnedDepth],
                      leftChild: Option[ActorRef[Command]],
                      rightChild: Option[ActorRef[Command]])
  extends AbstractBehavior[Command](context) {

  private var leftChildDepth: Option[Int] = None
  private var rightChildDepth: Option[Int] = None

  private def nextBehavior(): Behavior[Command] =
    (leftChildDepth, rightChildDepth) match {
      case (Some(ld), Some(rd)) =>
        replyTo ! ReturnedDepth(Math.max(ld, rd))
        Behaviors.stopped
      case _ =>
        Behaviors.same
    }

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Depth() =>
        context.self ! LeftDepth(context.self, leftChild)
        context.self ! RightDepth(context.self, rightChild)
        nextBehavior()
      case LeftDepth(replyTo, None) =>
        replyTo ! ReturnedLeftDepth(0)
        nextBehavior()
      case LeftDepth(replyTo, Some(leftChild)) =>
        leftChild ! BinaryTree.Depth(replyTo)
        nextBehavior()
      case RightDepth(replyTo, None) =>
        replyTo ! ReturnedRightDepth(0)
        nextBehavior()
      case RightDepth(replyTo, Some(rightChild)) =>
        rightChild ! BinaryTree.Depth(replyTo)
        nextBehavior()
      case ReturnedLeftDepth(value) =>
        leftChildDepth = Some(value)
        nextBehavior()
      case ReturnedRightDepth(value) =>
        rightChildDepth = Some(value)
        nextBehavior()
      case ReturnedDepth(value) =>
        replyTo ! ReturnedDepth(1 + value)
        Behaviors.stopped
    }
  }
}
