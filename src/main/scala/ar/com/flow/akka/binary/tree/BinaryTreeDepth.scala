package ar.com.flow.akka.binary.tree

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ar.com.flow.akka.binary.tree.BinaryTree.ReturnedDepth
import ar.com.flow.akka.binary.tree.BinaryTreeDepth._

object BinaryTreeDepth {
  final case class Depth() extends BinaryTree.Command

  final case class ReturnedLeftDepth(depth: Int = 0) extends BinaryTree.Command

  final case class ReturnedRightDepth(depth: Int = 0) extends BinaryTree.Command

  def apply(replyTo: ActorRef[BinaryTree.ReturnedDepth],
            leftChild: Option[ActorRef[BinaryTree.Command]] = None,
            rightChild: Option[ActorRef[BinaryTree.Command]] = None): Behavior[BinaryTree.Command] =
    Behaviors.setup(context => new BinaryTreeDepth(context, replyTo, leftChild, rightChild))
}

class BinaryTreeDepth(context: ActorContext[BinaryTree.Command],
                      replyTo: ActorRef[BinaryTree.ReturnedDepth],
                      leftChild: Option[ActorRef[BinaryTree.Command]],
                      rightChild: Option[ActorRef[BinaryTree.Command]]
                     )
  extends AbstractBehavior[BinaryTree.Command](context) {

  private var leftChildDepth: Option[Int] = None
  private var rightChildDepth: Option[Int] = None

  private def nextBehavior(): Behavior[BinaryTree.Command] =
    (leftChildDepth, rightChildDepth) match {
      case (Some(ld), Some(rd)) =>
        replyTo ! ReturnedDepth(1 + Math.max(ld, rd))
        Behaviors.stopped
      case _ =>
        Behaviors.same
    }

  override def onMessage(message: BinaryTree.Command): Behavior[BinaryTree.Command] = {
    message match {
      case Depth() =>
        val leftDepthCalculator = context.spawn(BranchDepth(), "left-depth")
        leftDepthCalculator ! BranchDepth.LeftDepth(context.self, leftChild)
        val rightDepthCalculator = context.spawn(BranchDepth(), "right-depth")
        rightDepthCalculator ! BranchDepth.RightDepth(context.self, rightChild)
        nextBehavior()
      case ReturnedLeftDepth(value) =>
        leftChildDepth = Some(value)
        nextBehavior()
      case ReturnedRightDepth(value) =>
        rightChildDepth = Some(value)
        nextBehavior()
      case ReturnedDepth(value) =>
        replyTo ! ReturnedDepth(1 + value)
        Behaviors.stopped
    }
  }
}
