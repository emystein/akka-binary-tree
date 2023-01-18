package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.{Command, ReturnedDepth}
import ar.com.flow.akka.binary.tree.BinaryTreeDepth._

object BinaryTreeDepth {
  final case class Depth() extends Command

  final case class LeftDepth(replyTo: ActorRef[Command],
                             leftChild: Option[ActorRef[Command]] = None) extends Command

  final case class ReachedLeftLeaf() extends Command

  final case class RightDepth(replyTo: ActorRef[Command],
                              rightChild: Option[ActorRef[Command]] = None) extends Command

  final case class ReachedRightLeaf() extends Command

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

  private var atLeftLeaf: Boolean = false
  private var atRightLeaf: Boolean = false

  private def nextBehavior(): Behavior[Command] =
    (atLeftLeaf, atRightLeaf) match {
      case (true, true) =>
        replyTo ! ReturnedDepth(0)
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
        replyTo ! ReachedLeftLeaf()
        nextBehavior()
      case LeftDepth(replyTo, Some(leftChild)) =>
        leftChild ! BinaryTree.Depth(replyTo)
        nextBehavior()
      case RightDepth(replyTo, None) =>
        replyTo ! ReachedRightLeaf()
        nextBehavior()
      case RightDepth(replyTo, Some(rightChild)) =>
        rightChild ! BinaryTree.Depth(replyTo)
        nextBehavior()
      case ReachedLeftLeaf() =>
        atLeftLeaf = true
        nextBehavior()
      case ReachedRightLeaf() =>
        atRightLeaf = true
        nextBehavior()
      case ReturnedDepth(value) =>
        replyTo ! ReturnedDepth(1 + value)
        Behaviors.stopped
    }
  }
}
