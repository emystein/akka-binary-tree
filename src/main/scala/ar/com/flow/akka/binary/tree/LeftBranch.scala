package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeDepth.ReachedLeftLeaf
import ar.com.flow.akka.binary.tree.LeftBranch.Depth

object LeftBranch {
  final case class Depth(replyTo: ActorRef[Command],
                         leftChild: Option[ActorRef[Command]] = None) extends Command


  def apply(): Behavior[Command] =
    Behaviors.setup(context => new LeftBranch(context))
}

class LeftBranch(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Depth(replyTo, None) =>
        replyTo ! ReachedLeftLeaf()
        this
      case Depth(replyTo, Some(leftChild)) =>
        leftChild ! BinaryTree.Depth(replyTo)
        this
    }
  }
}
