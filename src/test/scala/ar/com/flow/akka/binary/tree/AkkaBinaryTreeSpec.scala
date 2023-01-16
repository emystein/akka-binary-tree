package ar.com.flow.akka.binary.tree

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
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
      val replyProbe = createTestProbe[NodeReply]()
      val tree = spawn(BinaryTree())
      tree ! LeftChild(replyProbe.ref)
      replyProbe.expectMessage(NodeReply(None))
    }
    "not have Right child" in {
      val replyProbe = createTestProbe[NodeReply]()
      val tree = spawn(BinaryTree())
      tree ! RightChild(replyProbe.ref)
      replyProbe.expectMessage(NodeReply(None))
    }
    "not have parent" in {
      val replyProbe = createTestProbe[NodeReply]()
      val tree = spawn(BinaryTree())
      tree ! Parent(replyProbe.ref)
      replyProbe.expectMessage(NodeReply(None))
    }
  }
  "A Node" must {
    "have value" in {
      val replyProbe = createTestProbe[ValueReply]()
      val tree = spawn(BinaryTree(value=1))
      tree ! Value(replyProbe.ref)
      replyProbe.expectMessage(ValueReply(1))
    }
    "have added Left child" in {
      val tree = spawn(BinaryTree(value = 1))
      tree ! AddLeftChild(value = 2, leftChild=None, rightChild = None)
      expectLeftValue(tree, 2)
    }
    "have added Right child" in {
      val tree = spawn(BinaryTree(value = 1))
      tree ! AddRightChild(value = 3, leftChild = None, rightChild = None)
      expectRightValue(tree, 3)
    }
    "have Root path" in {
      val tree = spawn(BinaryTree(value = 1))
      val pathReply = createTestProbe[PathReply]()
      tree ! Path(pathReply.ref)
      pathReply.expectMessage(PathReply("/"))
    }
    "have Left child path" in {
      val tree = spawn(BinaryTree(value = 1, parent=None, leftChild=Some(NodeState(2, None, None)), rightChild=Some(NodeState(3, None, None))))

      val leftChildReplyProbe = createTestProbe[NodeReply]()
      tree ! LeftChild(leftChildReplyProbe.ref)
      val leftChild = leftChildReplyProbe.receiveMessage()

      val pathReply = createTestProbe[PathReply]()
      leftChild.actorRef.get ! Path(pathReply.ref)
      pathReply.expectMessage(PathReply("/left"))
    }
  }
  "A Child Node" must {
    "have parent" in {
      val tree = spawn(BinaryTree(value = 1, parent=None, leftChild=Some(NodeState(2, None, None)), rightChild=Some(NodeState(3, None, None))))
      expectLeftHasParent(tree)
      expectRightHasParent(tree)
    }
  }
  "A Binary Tree" must {
    "construct with Left and Right children" in {
      val tree = spawn(BinaryTree(value = 1, parent=None, leftChild=Some(NodeState(2, None, None)), rightChild=Some(NodeState(3, None, None))))
      expectLeftValue(tree, 2)
      expectRightValue(tree, 3)
    }
  }

  private def expectLeftValue(tree: ActorRef[Command], expectedValue: Int) = {
    val replyProbe = createTestProbe[NodeReply]()
    val message = LeftChild(replyProbe.ref)
    expectNodeValue(tree, message, replyProbe, expectedValue)
  }

  private def expectRightValue(tree: ActorRef[Command], expectedValue: Int) = {
    val rightChildReplyProbe = createTestProbe[NodeReply]()
    val rightChildMessage = RightChild(rightChildReplyProbe.ref)
    expectNodeValue(tree, rightChildMessage, rightChildReplyProbe, expectedValue)
  }

  private def expectNodeValue(tree: ActorRef[Command], childMessage: Command, childReplyProbe: TestProbe[NodeReply], expectedValue: Int) = {
    tree ! childMessage
    val rightChild = childReplyProbe.receiveMessage()
    val rightValueReply = createTestProbe[ValueReply]()
    rightChild.actorRef.get ! Value(rightValueReply.ref)
    rightValueReply.expectMessage(ValueReply(expectedValue))
  }

  private def expectLeftHasParent(tree: ActorRef[Command]): Unit = {
    val childReply = createTestProbe[NodeReply]()
    tree ! LeftChild(childReply.ref)
    expectNodeHasParent(childReply, tree)
  }

  private def expectRightHasParent(tree: ActorRef[Command]): Unit = {
    val childReply = createTestProbe[NodeReply]()
    tree ! RightChild(childReply.ref)
    expectNodeHasParent(childReply, tree)
  }

  private def expectNodeHasParent(reply: TestProbe[NodeReply], expectedParent: ActorRef[Command]) = {
    val child = reply.receiveMessage().actorRef.get
    val parentReply = createTestProbe[NodeReply]()
    child ! Parent(parentReply.ref)
    parentReply.expectMessage(NodeReply(Some(expectedParent)))
  }
}
