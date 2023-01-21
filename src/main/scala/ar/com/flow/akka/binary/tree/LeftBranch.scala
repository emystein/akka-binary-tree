package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.Command
import ar.com.flow.akka.binary.tree.BinaryTreeHeight.ReachedLeftLeaf
import ar.com.flow.akka.binary.tree.LeftBranch.Height

object LeftBranch {
  final case class Height(replyTo: ActorRef[Command],
                         leftChild: Option[ActorRef[Command]] = None) extends Command


  def apply(): Behavior[Command] = Behaviors.setup(context => new LeftBranch(context))
}

class LeftBranch(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Height(replyTo, None) =>
        replyTo ! ReachedLeftLeaf()
        this
      case Height(replyTo, Some(leftChild)) =>
        leftChild ! BinaryTree.Height(replyTo)
        this
    }
  }
}
