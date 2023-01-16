package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.{Path, PathReply}
import ar.com.flow.akka.binary.tree.BinaryTreePath.TreePath

object BinaryTreePath {
  sealed trait Command

  final case class TreePath(replyTo: ActorRef[PathReply], parent: Option[ActorRef[BinaryTree.Command]] = None, name: String, collectedPath: Option[String] = None) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreePath(context))
}

class BinaryTreePath(context: ActorContext[BinaryTreePath.Command])
  extends AbstractBehavior[BinaryTreePath.Command](context) {
  override def onMessage(message: BinaryTreePath.Command): Behavior[BinaryTreePath.Command] = {
    message match {
      case TreePath(replyTo, None, name, None) =>
        replyTo ! PathReply("/")
        this
      case TreePath(replyTo, None, name, Some(collectedPath)) =>
        replyTo ! PathReply(s"/$collectedPath")
        this
      case TreePath(replyTo, Some(parent), name, None) =>
        parent ! Path(replyTo, Some(s"$name"))
        this
      case TreePath(replyTo, Some(parent), name, Some(collectedPath)) =>
        parent ! Path(replyTo, Some(s"$name/$collectedPath"))
        this
    }
  }
}