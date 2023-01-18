package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeDepth.ReachedRightLeaf
import ar.com.flow.akka.binary.tree.RightBranch.Depth

object RightBranch {
  final case class Depth(replyTo: ActorRef[Command],
                         rightChild: Option[ActorRef[Command]] = None) extends Command

  def apply(): Behavior[Command] = Behaviors.setup(context => new RightBranch(context))
}

class RightBranch(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Depth(replyTo, None) =>
        replyTo ! ReachedRightLeaf()
        this
      case Depth(replyTo, Some(rightChild)) =>
        rightChild ! BinaryTree.Depth(replyTo)
        this
    }
  }
}
