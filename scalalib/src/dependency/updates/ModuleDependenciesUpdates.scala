package mill.scalalib.dependency.updates

import mill.scalalib.JavaModule
import mill.scalalib.dependency.versions.Version

import scala.collection.SortedSet

private[dependency] final case class ModuleDependenciesUpdates(
    modulePath: String,
    dependencies: Seq[DependencyUpdates])

private[dependency] final case class DependencyUpdates(
    dependencyName: String,
    currentVersion: Version,
    updates: SortedSet[Version])
