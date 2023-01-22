package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeHeight._

object BinaryTreeHeight {
  final case class Height(replyTo: ActorRef[ReturnedHeight], accumulatedHeight: Int) extends Command
  final case class ReturnedHeight(height: Int) extends Command

  final case class ReachedLeaf(height: Int) extends Command

  def apply(leftChild: Option[ActorRef[Command]] = None,
            rightChild: Option[ActorRef[Command]] = None): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreeHeight(context, leftChild, rightChild))
}

class BinaryTreeHeight(context: ActorContext[Command],
                       leftChild: Option[ActorRef[Command]],
                       rightChild: Option[ActorRef[Command]])
  extends AbstractBehavior[Command](context) {

  private var replyTo: ActorRef[ReturnedHeight] = context.self
  private var childrenHeights: Seq[Int] = Seq()

  private def nextBehavior(): Behavior[Command] =
    if (childrenHeights.size == 2) {
      this.replyTo ! ReturnedHeight(childrenHeights.max)
      Behaviors.stopped
    } else {
      Behaviors.same
    }

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Height(replyTo, accumulatedHeight) =>
        this.replyTo = replyTo
        leftBranch ! Branch.Height(replyTo = context.self, leftChild, accumulatedHeight)
        rightBranch ! Branch.Height(replyTo = context.self, rightChild, accumulatedHeight)
        nextBehavior()
      case ReachedLeaf(accumulatedHeight) =>
        this.childrenHeights = accumulatedHeight +: this.childrenHeights
        nextBehavior()
      case ReturnedHeight(value) =>
        this.replyTo ! ReturnedHeight(value)
        Behaviors.stopped
    }
  }

  private def leftBranch: ActorRef[Command] = {
    context.spawn(Branch(), "left-height")
  }

  private def rightBranch: ActorRef[Command] = {
    context.spawn(Branch(), "right-height")
  }
}

object Branch {
  final case class Height(replyTo: ActorRef[Command],
                          node: Option[ActorRef[Command]] = None,
                          accumulatedHeight: Int) extends Command

  def apply(): Behavior[Command] = Behaviors.setup(context => new Branch(context))
}

class Branch(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Branch.Height(replyTo, None, accumulatedHeight) =>
        replyTo ! ReachedLeaf(accumulatedHeight + 1)
        this
      case Branch.Height(replyTo, Some(node), accumulatedHeight) =>
        node ! BinaryTree.Height(replyTo, accumulatedHeight + 1)
        this
    }
  }
}
