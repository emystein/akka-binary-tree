package ar.com.flow.akka.binary.tree

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import ar.com.flow.akka.binary.tree.Node._
import org.scalatest.wordspec.AnyWordSpecLike

class AkkaBinaryTreeSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A Leaf" must {
    "have depth equal to 0" in {
      val replyProbe = createTestProbe[DepthReply]()
      val tree = spawn(Node())
      tree ! Depth(replyProbe.ref)
      replyProbe.expectMessage(DepthReply(1))
    }
    "not have Left child" in {
      val replyProbe = createTestProbe[NodeReply]()
      val tree = spawn(Node())
      tree ! LeftChild(replyProbe.ref)
      replyProbe.expectMessage(NodeReply(None))
    }
    "not have Right child" in {
      val replyProbe = createTestProbe[NodeReply]()
      val tree = spawn(Node())
      tree ! RightChild(replyProbe.ref)
      replyProbe.expectMessage(NodeReply(None))
    }
  }
  "A Node" must {
    "have value" in {
      val replyProbe = createTestProbe[ValueReply]()
      val tree = spawn(Node(value=1))
      tree ! Value(replyProbe.ref)
      replyProbe.expectMessage(ValueReply(1))
    }
  }
}
