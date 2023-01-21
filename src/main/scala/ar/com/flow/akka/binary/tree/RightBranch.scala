package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeHeight.ReachedRightLeaf
import ar.com.flow.akka.binary.tree.RightBranch.Height

object RightBranch {
  final case class Height(replyTo: ActorRef[Command],
                          rightChild: Option[ActorRef[Command]] = None,
                          accumulatedHeight: Int) extends Command

  def apply(): Behavior[Command] = Behaviors.setup(context => new RightBranch(context))
}

class RightBranch(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Height(replyTo, None, accumulatedHeight) =>
        replyTo ! ReachedRightLeaf(accumulatedHeight)
        this
      case Height(replyTo, Some(rightChild), accumulatedHeight) =>
        rightChild ! BinaryTree.Height(replyTo, accumulatedHeight + 1)
        this
    }
  }
}
