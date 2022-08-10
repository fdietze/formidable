package webapp

import formidable._

trait Extras {
  // needed for recursion with Scala 3 (https://github.com/softwaremill/magnolia#limitations)
  implicit def treeInstance: Form[Tree]                          = Form.derived
  implicit def binaryTreeInstance: Form[BinaryTree]              = Form.derived
  implicit def genericListInstance: Form[GenericLinkedList[Pet]] = Form.derived
}
