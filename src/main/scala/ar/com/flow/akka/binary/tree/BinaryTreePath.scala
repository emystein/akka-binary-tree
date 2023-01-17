package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.ReturnedPath
import ar.com.flow.akka.binary.tree.BinaryTreePath.{Command, Path}

object BinaryTreePath {
  sealed trait Command

  final case class Path(replyTo: ActorRef[ReturnedPath],
                        parent: Option[ActorRef[BinaryTree.Command]] = None,
                        name: String, collectedPath: Option[String] = None) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreePath(context))
}

class BinaryTreePath(context: ActorContext[Command])
  extends AbstractBehavior[Command](context) {
  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Path(replyTo, None, _, collectedPath) =>
        replyTo ! ReturnedPath(s"/${collectedPath.getOrElse("")}")
        this
      case Path(replyTo, Some(parent), name, None) =>
        parent ! BinaryTree.Path(replyTo, Some(s"$name"))
        this
      case Path(replyTo, Some(parent), name, Some(collectedPath)) =>
        parent ! BinaryTree.Path(replyTo, Some(s"$name/$collectedPath"))
        this
    }
  }
}
