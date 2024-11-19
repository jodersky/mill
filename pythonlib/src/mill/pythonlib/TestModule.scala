package mill.pythonlib

import mill.define.TaskModule
import mill.define.Command

trait TestModule extends PythonModule with TaskModule {

  override def defaultCommandName(): String = "test"

  // TODO: make this return something more useful
  def test(args: String*): Command[Unit]

}

object TestModule {

  trait UnittestModule extends TestModule {



    // python -m unittest test_module1 test_module2

  }

  trait PytestModule {

  }

}
