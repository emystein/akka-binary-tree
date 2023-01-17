package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.ReturnedDepth
import ar.com.flow.akka.binary.tree.BinaryTreeDepth.{Command, LeftDepth, ReturnedLeftDepth, ReturnedRightDepth, RightDepth}

object BinaryTreeDepth {
  sealed trait Command

  final case class Depth(replyTo: ActorRef[ReturnedDepth],
                         leftChild: Option[ActorRef[BinaryTree.Command]] = None,
                         rightChild: Option[ActorRef[BinaryTree.Command]] = None,
                         collectedDepth: Int = 0
                        ) extends Command
  final case class LeftDepth(replyTo: ActorRef[ReturnedLeftDepth],
                             leftChild: Option[ActorRef[BinaryTree.Command]] = None) extends Command
  final case class ReturnedLeftDepth(depth: Int = 0) extends Command
  final case class RightDepth(replyTo: ActorRef[ReturnedRightDepth],
                              rightChild: Option[ActorRef[BinaryTree.Command]] = None) extends Command
  final case class ReturnedRightDepth(depth: Int = 0) extends Command

  def apply(replyTo: ActorRef[BinaryTree.Command],
            leftChild: Option[ActorRef[BinaryTree.Command]] = None,
            rightChild: Option[ActorRef[BinaryTree.Command]] = None): Behavior[Command] =
    Behaviors.setup(context => new BinaryTreeDepth(context, replyTo, leftChild, rightChild))
}

class BinaryTreeDepth(context: ActorContext[Command],
                      replyTo: ActorRef[BinaryTree.Command],
                      leftChild: Option[ActorRef[BinaryTree.Command]],
                      rightChild: Option[ActorRef[BinaryTree.Command]]
                     )
  extends AbstractBehavior[Command](context) {

  val leftChildDepthCalculator: ActorRef[Command] = context.spawn(BinaryTreeDepth(replyTo, leftChild, None), "left-depth")
  val rightChildDepthCalculator: ActorRef[Command] = context.spawn(BinaryTreeDepth(replyTo, None, rightChild), "right-depth")

  var leftChildDepth: Option[Int] = None
  var rightChildDepth: Option[Int] = None

  leftChildDepthCalculator ! LeftDepth(context.self)
  rightChildDepthCalculator ! RightDepth(context.self)

  def nextBehavior(): Behavior[Command] =
    (leftChildDepth, rightChildDepth) match {
      case (Some(ld), Some(rd)) =>
        replyTo ! ReturnedDepth(Math.max(ld, rd))
        Behaviors.stopped
      case _ =>
        Behaviors.same
    }

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
//      case Depth(replyTo, None, None, collectedDepth) =>
//        replyTo ! ReturnedDepth(collectedDepth + 1)
//        this
//      case Depth(replyTo, Some(leftChild), None, collectedDepth) =>
//        context.self ! Depth(replyTo, )
//        this
//      case Depth(replyTo, None, Some(rightChild), collectedDepth) =>
//        parent ! BinaryTree.Depth(replyTo, Some(s"$name"))
//        this
//      case Depth(replyTo, Some(leftChile), Some(rightChild), collectedDepth) =>
//        parent ! BinaryTree.Depth(replyTo, Some(s"$name"))
//        this
      case ReturnedLeftDepth(value) =>
        leftChildDepth = Some(value)
        nextBehavior()
      case ReturnedRightDepth(value) =>
        rightChildDepth = Some(value)
        nextBehavior()
      case _ =>
        Behaviors.unhandled
    }
  }
}
