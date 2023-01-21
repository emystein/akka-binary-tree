package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeHeight._

object BinaryTreeHeight {
  final case class Height() extends Command

  final case class ReachedLeftLeaf() extends Command

  final case class ReachedRightLeaf() extends Command

  final case class ReturnedHeight(height: Int) extends Command

  def apply(replyTo: ActorRef[ReturnedHeight],
            leftChild: Option[ActorRef[Command]] = None,
            rightChild: Option[ActorRef[Command]] = None): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreeHeight(context, replyTo, leftChild, rightChild))
}

class BinaryTreeHeight(context: ActorContext[Command],
                       replyTo: ActorRef[ReturnedHeight],
                       leftChild: Option[ActorRef[Command]],
                       rightChild: Option[ActorRef[Command]])
  extends AbstractBehavior[Command](context) {

  private var atLeftLeaf: Boolean = false
  private var atRightLeaf: Boolean = false

  private def nextBehavior(): Behavior[Command] =
    (atLeftLeaf, atRightLeaf) match {
      case (true, true) =>
        replyTo ! ReturnedHeight(1)
        Behaviors.stopped
      case _ =>
        Behaviors.same
    }

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Height() =>
        leftBranch ! LeftBranch.Height(context.self, leftChild)
        rightBranch ! RightBranch.Height(context.self, rightChild)
        nextBehavior()
      case ReachedLeftLeaf() =>
        atLeftLeaf = true
        nextBehavior()
      case ReachedRightLeaf() =>
        atRightLeaf = true
        nextBehavior()
      case ReturnedHeight(value) =>
        replyTo ! ReturnedHeight(1 + value)
        Behaviors.stopped
    }
  }

  private def leftBranch: ActorRef[Command] = {
    context.spawn(LeftBranch(), "left-height")
  }

  private def rightBranch: ActorRef[Command] = {
    context.spawn(RightBranch(), "right-height")
  }
}
