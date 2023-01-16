package ar.com.flow.akka.binary.tree

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import ar.com.flow.akka.binary.tree.BinaryTree._
import org.scalatest.wordspec.AnyWordSpecLike

class AkkaBinaryTreeSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A Leaf" must {
    "have depth equal to 0" in {
      val replyProbe = createTestProbe[DepthReturned]()
      val tree = spawn(BinaryTree())
      tree ! Depth(replyProbe.ref)
      replyProbe.expectMessage(DepthReturned(1))
    }
    "not have Left child" in {
      val replyProbe = createTestProbe[NodeReturned]()
      val tree = spawn(BinaryTree())
      tree ! LeftChild(replyProbe.ref)
      replyProbe.expectMessage(NodeReturned(None))
    }
    "not have Right child" in {
      val replyProbe = createTestProbe[NodeReturned]()
      val tree = spawn(BinaryTree())
      tree ! RightChild(replyProbe.ref)
      replyProbe.expectMessage(NodeReturned(None))
    }
    "not have parent" in {
      val replyProbe = createTestProbe[NodeReturned]()
      val tree = spawn(BinaryTree())
      tree ! Parent(replyProbe.ref)
      replyProbe.expectMessage(NodeReturned(None))
    }
  }
  "A Node" must {
    "have value" in {
      val replyProbe = createTestProbe[ValueReturned]()
      val tree = spawn(BinaryTree(value=1))
      tree ! Value(replyProbe.ref)
      replyProbe.expectMessage(ValueReturned(1))
    }
    "have added Left child" in {
      val tree = spawn(BinaryTree(value = 1))

      val replyProbe = createTestProbe[NodeReturned]()
      tree ! AddLeftChild(replyProbe.ref, value = 2, leftChild=None, rightChild = None)
      expectLeftValue(tree, 2)
    }
    "have added Right child" in {
      val tree = spawn(BinaryTree(value = 1))

      val replyProbe = createTestProbe[NodeReturned]()
      tree ! AddRightChild(replyProbe.ref, value = 3, leftChild = None, rightChild = None)
      expectRightValue(tree, 3)
    }
    "have Left child path" in {
      val treeActor = BinaryTree(value = 1,
        leftChild = Some(Node(2, None, None)),
        rightChild = Some(Node(3, None, None))
      )

      val tree = spawn(treeActor)

      val nodeReplyProbe = createTestProbe[NodeReturned]()
      tree ! LeftChild(nodeReplyProbe.ref)
      val leftChild = nodeReplyProbe.receiveMessage()

      val pathReply = createTestProbe[PathReturned]()
      leftChild.node.get ! Path(pathReply.ref)
      pathReply.expectMessage(PathReturned("/left"))
    }
    "have Left/Left child path" in {
      val treeBehavior = BinaryTree(value = 1,
        leftChild = Some(Node(2,
          leftChild = Some(Node(3, None, None)),
          rightChild = None)),
        rightChild = None)

      val tree = spawn(treeBehavior)

      val nodeReply = createTestProbe[NodeReturned]()
      tree ! LeftChild(nodeReply.ref)
      val leftChild = nodeReply.receiveMessage()

      leftChild.node.get ! LeftChild(nodeReply.ref)
      val leftLeftChild = nodeReply.receiveMessage()

      val pathReply = createTestProbe[PathReturned]()
      leftLeftChild.node.get ! Path(pathReply.ref)
      pathReply.expectMessage(PathReturned("/left/left"))
    }
    "have Left/right child path" in {
      val treeBehavior = BinaryTree(value = 1,
        leftChild = Some(Node(2,
          leftChild = None,
          rightChild = Some(Node(3, None, None)),
        )),
        rightChild = None)

      val tree = spawn(treeBehavior)

      val nodeReply = createTestProbe[NodeReturned]()
      tree ! LeftChild(nodeReply.ref)
      val leftChild = nodeReply.receiveMessage()

      leftChild.node.get ! RightChild(nodeReply.ref)
      val leftRightChild = nodeReply.receiveMessage()

      val pathReply = createTestProbe[PathReturned]()
      leftRightChild.node.get ! Path(pathReply.ref)
      pathReply.expectMessage(PathReturned("/left/right"))
    }
  }
  "The Root Node" must {
    "have / path" in {
      val tree = spawn(BinaryTree(value = 1))
      val pathReply = createTestProbe[PathReturned]()
      tree ! Path(pathReply.ref)
      pathReply.expectMessage(PathReturned("/"))
    }
  }
  "A Child Node" must {
    "have parent" in {
      val treeBehavior = BinaryTree(value = 1,
        leftChild = Some(Node(2, None, None)),
        rightChild = Some(Node(3, None, None))
      )

      val tree = spawn(treeBehavior)

      expectLeftHasParent(tree)
      expectRightHasParent(tree)
    }
  }
  "A Binary Tree" must {
    "construct with Left and Right children" in {
      val treeBehavior = BinaryTree(value = 1,
        leftChild = Some(Node(2, None, None)),
        rightChild = Some(Node(3, None, None))
      )

      val tree = spawn(treeBehavior)

      expectLeftValue(tree, 2)
      expectRightValue(tree, 3)
    }
  }

  private def expectLeftValue(tree: ActorRef[Command], expectedValue: Int) = {
    val replyProbe = createTestProbe[NodeReturned]()
    val message = LeftChild(replyProbe.ref)
    expectNodeValue(tree, message, replyProbe, expectedValue)
  }

  private def expectRightValue(tree: ActorRef[Command], expectedValue: Int) = {
    val rightChildReplyProbe = createTestProbe[NodeReturned]()
    val rightChildMessage = RightChild(rightChildReplyProbe.ref)
    expectNodeValue(tree, rightChildMessage, rightChildReplyProbe, expectedValue)
  }

  private def expectNodeValue(tree: ActorRef[Command], childMessage: Command, childReplyProbe: TestProbe[NodeReturned], expectedValue: Int) = {
    tree ! childMessage
    val rightChild = childReplyProbe.receiveMessage()
    val rightValueReply = createTestProbe[ValueReturned]()
    rightChild.node.get ! Value(rightValueReply.ref)
    rightValueReply.expectMessage(ValueReturned(expectedValue))
  }

  private def expectLeftHasParent(tree: ActorRef[Command]): Unit = {
    val childReply = createTestProbe[NodeReturned]()
    tree ! LeftChild(childReply.ref)
    expectNodeHasParent(childReply, tree)
  }

  private def expectRightHasParent(tree: ActorRef[Command]): Unit = {
    val childReply = createTestProbe[NodeReturned]()
    tree ! RightChild(childReply.ref)
    expectNodeHasParent(childReply, tree)
  }

  private def expectNodeHasParent(reply: TestProbe[NodeReturned], expectedParent: ActorRef[Command]) = {
    val child = reply.receiveMessage().node.get
    val parentReply = createTestProbe[NodeReturned]()
    child ! Parent(parentReply.ref)
    parentReply.expectMessage(NodeReturned(Some(expectedParent)))
  }
}
