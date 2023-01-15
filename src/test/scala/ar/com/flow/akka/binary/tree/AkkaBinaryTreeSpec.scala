package ar.com.flow.akka.binary.tree

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import ar.com.flow.akka.binary.tree.BinaryTree._
import org.scalatest.wordspec.AnyWordSpecLike

class AkkaBinaryTreeSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A Leaf" must {
    "have depth equal to 0" in {
      val replyProbe = createTestProbe[DepthReply]()
      val tree = spawn(BinaryTree())
      tree ! Depth(replyProbe.ref)
      replyProbe.expectMessage(DepthReply(1))
    }
    "not have Left child" in {
      val replyProbe = createTestProbe[NodeState]()
      val tree = spawn(BinaryTree())
      tree ! LeftChild(replyProbe.ref)
      replyProbe.expectMessage(NodeState(0, None, None))
    }
    "not have Right child" in {
      val replyProbe = createTestProbe[NodeState]()
      val tree = spawn(BinaryTree())
      tree ! RightChild(replyProbe.ref)
      replyProbe.expectMessage(NodeState(0, None, None))
    }
  }
  "A Node" must {
    "have value" in {
      val replyProbe = createTestProbe[ValueReply]()
      val tree = spawn(BinaryTree(value=1))
      tree ! Value(replyProbe.ref)
      replyProbe.expectMessage(ValueReply(1))
    }
    "have Left child" in {
      val tree = spawn(BinaryTree(value = 1))
      tree ! AddLeftChild(value = 2, leftChild=None, rightChild = None)

      val leftChildReplyProbe = createTestProbe[NodeState]()
      tree ! LeftChild(leftChildReplyProbe.ref)

      val leftChild = NodeState(value = 2, None, None)
      leftChildReplyProbe.expectMessage(leftChild)
    }
    "have Right child" in {
      val tree = spawn(BinaryTree(value = 1))
      tree ! AddRightChild(value = 2, leftChild = None, rightChild = None)

      val childReplyProbe = createTestProbe[NodeState]()
      tree ! RightChild(childReplyProbe.ref)

      val rightChild = NodeState(value = 2, None, None)
      childReplyProbe.expectMessage(rightChild)
    }
  }
  "A Binary Tree" must {
    "have Left and Right children" in {
      val leftChild = Some(NodeState(2, None, None))
      val rightChild = Some(NodeState(3, None, None))
      val tree = spawn(BinaryTree(value = 1, leftChild, rightChild))

      val replyProbe = createTestProbe[ValueReply]()
      tree ! Value(replyProbe.ref)
      replyProbe.expectMessage(ValueReply(1))

      val leftChildReplyProbe = createTestProbe[NodeState]()
      tree ! LeftChild(leftChildReplyProbe.ref)
      leftChildReplyProbe.expectMessage(NodeState(2, None, None))

      val rightChildReplyProbe = createTestProbe[NodeState]()
      tree ! RightChild(rightChildReplyProbe.ref)
      rightChildReplyProbe.expectMessage(NodeState(3, None, None))
    }
  }
}
