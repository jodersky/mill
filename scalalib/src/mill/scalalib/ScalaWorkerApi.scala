package mill.scalalib


import ammonite.ops.Path
import coursier.Cache
import coursier.maven.MavenRepository
import mill.Agg
import mill.scalalib.TestRunner.Result
import mill.T
import mill.define.Worker
import mill.scalalib.Lib.resolveDependencies
import mill.util.Loose
import mill.util.JsonFormatters._
object ScalaWorkerApi extends mill.define.ExternalModule {
  def scalaWorkerClasspath = T{
    val scalaWorkerJar = sys.props("MILL_SCALA_WORKER")
    if (scalaWorkerJar != null) Loose.Agg.from(scalaWorkerJar.split(',').map(Path(_)))
    else {
      val mill.eval.Result.Success(v) = resolveDependencies(
        Seq(Cache.ivy2Local, MavenRepository("https://repo1.maven.org/maven2")),
        "2.12.4",
        Seq(ivy"com.lihaoyi::mill-scalaworker:" + sys.props("MILL_VERSION"))
      )
      v.map(_.path)
    }
  }
  def scalaWorker: Worker[ScalaWorkerApi] = T.worker{
    val cl = new java.net.URLClassLoader(
      scalaWorkerClasspath().map(_.toNIO.toUri.toURL).toArray,
      getClass.getClassLoader
    )
    val cls = cl.loadClass("mill.scalaworker.ScalaWorker")
    val instance = cls.getConstructor(classOf[mill.util.Ctx], classOf[Array[String]])
      .newInstance(T.ctx(), compilerInterfaceClasspath().map(_.path.toString).toArray[String])
    instance.asInstanceOf[ScalaWorkerApi]
  }

  def compilerInterfaceClasspath = T{
    resolveDependencies(
      Seq(Cache.ivy2Local, MavenRepository("https://repo1.maven.org/maven2")),
      "2.12.4",
      Seq(ivy"org.scala-sbt:compiler-interface:1.1.0")
    )
  }
}

trait ScalaWorkerApi {
  def compileScala(scalaVersion: String,
                   sources: Agg[Path],
                   compileBridgeSources: Agg[Path],
                   compileClasspath: Agg[Path],
                   compilerClasspath: Agg[Path],
                   pluginClasspath: Agg[Path],
                   scalacOptions: Seq[String],
                   scalacPluginClasspath: Agg[Path],
                   javacOptions: Seq[String],
                   upstreamCompileOutput: Seq[CompilationResult])
                  (implicit ctx: mill.util.Ctx): CompilationResult

  def apply(frameworkInstance: ClassLoader => sbt.testing.Framework,
            entireClasspath: Agg[Path],
            testClassfilePath: Agg[Path],
            args: Seq[String])
           (implicit ctx: mill.util.Ctx.Log): (String, Seq[Result])
}
