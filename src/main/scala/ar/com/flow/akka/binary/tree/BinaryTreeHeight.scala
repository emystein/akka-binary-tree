package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeHeight._

object BinaryTreeHeight {
  final case class Height(replyTo: ActorRef[ReturnedHeight], accumulatedLeftHeight: Int) extends Command

  final case class ReachedLeftLeaf(height: Int) extends Command

  final case class ReachedRightLeaf(height: Int) extends Command

  final case class ReturnedHeight(height: Int) extends Command

  def apply(leftChild: Option[ActorRef[Command]] = None,
            rightChild: Option[ActorRef[Command]] = None): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreeHeight(context, leftChild, rightChild))
}

class BinaryTreeHeight(context: ActorContext[Command],
                       leftChild: Option[ActorRef[Command]],
                       rightChild: Option[ActorRef[Command]])
  extends AbstractBehavior[Command](context) {

  private var replyTo: ActorRef[ReturnedHeight] = context.self
  private var accumulatedHeight: Int = 0
  private var leftHeight: Option[Int] = None
  private var rightHeight: Option[Int] = None

  private def nextBehavior(): Behavior[Command] =
    (leftHeight, rightHeight) match {
      case (Some(leftValue), Some(rightValue)) =>
        replyTo ! ReturnedHeight(Math.max(1, accumulatedHeight + Math.max(leftValue, rightValue)))
        Behaviors.stopped
      case _ =>
        Behaviors.same
    }

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Height(replyTo, accumulatedHeight) =>
        this.replyTo = replyTo
        this.accumulatedHeight = accumulatedHeight
        leftBranch ! Branch.Height(replyTo = context.self, leftChild, accumulatedHeight)
        rightBranch ! Branch.Height(replyTo = context.self, rightChild, accumulatedHeight)
        nextBehavior()
      case ReachedLeftLeaf(accumulatedHeight) =>
        this.leftHeight = Some(accumulatedHeight)
        nextBehavior()
      case ReachedRightLeaf(accumulatedHeight) =>
        this.rightHeight = Some(accumulatedHeight)
        nextBehavior()
      case ReturnedHeight(value) =>
        this.replyTo ! ReturnedHeight(value)
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

object Branch {
  final case class Height(replyTo: ActorRef[Command],
                          node: Option[ActorRef[Command]] = None,
                          accumulatedHeight: Int) extends Command

}

class Branch(context: ActorContext[Command], heightReply: Int => Command) extends AbstractBehavior[Command](context) {
  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Branch.Height(replyTo, None, accumulatedHeight) =>
        replyTo ! this.heightReply(accumulatedHeight)
        this
      case Branch.Height(replyTo, Some(node), accumulatedHeight) =>
        node ! BinaryTree.Height(replyTo, accumulatedHeight + 1)
        this
    }
  }
}

object LeftBranch {
  def apply(): Behavior[Command] = Behaviors.setup(context => new Branch(context, heightReply))

  private def heightReply: Int => Command = height => ReachedLeftLeaf(height)
}

object RightBranch {
  def apply(): Behavior[Command] = Behaviors.setup(context => new Branch(context, heightReply))

  private def heightReply: Int => Command = height => ReachedRightLeaf(height)
}
