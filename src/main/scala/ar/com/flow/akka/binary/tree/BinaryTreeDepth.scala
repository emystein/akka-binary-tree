package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeDepth._

object BinaryTreeDepth {
  final case class Depth() extends Command

  final case class ReachedLeftLeaf() extends Command

  final case class ReachedRightLeaf() extends Command

  final case class ReturnedDepth(depth: Int) extends Command

  def apply(replyTo: ActorRef[ReturnedDepth],
            leftChild: Option[ActorRef[Command]] = None,
            rightChild: Option[ActorRef[Command]] = None): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreeDepth(context, replyTo, leftChild, rightChild))
}

class BinaryTreeDepth(context: ActorContext[Command],
                      replyTo: ActorRef[ReturnedDepth],
                      leftChild: Option[ActorRef[Command]],
                      rightChild: Option[ActorRef[Command]])
  extends AbstractBehavior[Command](context) {

  private var atLeftLeaf: Boolean = false
  private var atRightLeaf: Boolean = false

  private def nextBehavior(): Behavior[Command] =
    (atLeftLeaf, atRightLeaf) match {
      case (true, true) =>
        replyTo ! ReturnedDepth(1)
        Behaviors.stopped
      case _ =>
        Behaviors.same
    }

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Depth() =>
        leftBranch ! LeftBranch.Depth(context.self, leftChild)
        rightBranch ! RightBranch.Depth(context.self, rightChild)
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

  private def leftBranch: ActorRef[Command] = {
    context.spawn(LeftBranch(), "left-depth")
  }

  private def rightBranch: ActorRef[Command] = {
    context.spawn(RightBranch(), "right-depth")
  }
}
