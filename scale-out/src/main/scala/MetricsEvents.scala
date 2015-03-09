package com.packt.akka

import akka.actor.{ ActorLogging, Actor, ActorSystem, Props }
import akka.cluster.Cluster
import akka.cluster.metrics.{ ClusterMetricsEvent, ClusterMetricsChanged, NodeMetrics, ClusterMetricsExtension }
import akka.cluster.metrics.StandardMetrics.{ HeapMemory, Cpu }

import com.typesafe.config.ConfigFactory

class MetricsListener extends Actor with ActorLogging {
  val selfAddress = Cluster(context.system).selfAddress
  val extension = ClusterMetricsExtension(context.system)

  // Subscribe unto ClusterMetricsEvent events.
  override def preStart(): Unit = extension.subscribe(self)

  // Unsubscribe from ClusterMetricsEvent events.
  override def postStop(): Unit = extension.unsubscribe(self)

  def receive = {
    case ClusterMetricsChanged(metrics) =>
      metrics.filter(_.address == selfAddress) foreach { nodeMetrics =>
        logHeap(nodeMetrics)
        logCpu(nodeMetrics)
      }
  }

  def logHeap(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case HeapMemory(address, timestamp, used, committed, max) =>
      log.info("Used heap: {} MB", used.doubleValue / 1024 / 1024)
    case _ => // No heap info.
  }

  def logCpu(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, cpuStolen, processors) =>
      log.info("Load: {} ({} processors)", systemLoadAverage, processors)
    case _ => // No cpu info.
  }
}

object Metrics extends App{
  val config = ConfigFactory.load.getConfig("Metrics")
  val system = ActorSystem("Metrics", config)

  system.actorOf(Props[MetricsListener])
  Thread.sleep(10*1000)

  system.shutdown()
}
