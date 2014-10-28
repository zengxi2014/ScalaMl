/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * Version 0.94
 */
package org.scalaml.app.chap12


import org.scalaml.core.XTSeries
import scala.util.Random
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.Config
import akka.actor.Props
import scala.concurrent.{Await, duration}
import org.scalaml.core.types.ScalaMl._
import org.scalaml.scalability.spark._
import org.apache.spark.SparkContext
import org.apache.spark.storage.StorageLevel
import org.scalaml.scalability.akka._
import org.scalaml.scalability.scala._
import org.scalaml.util.Display
import org.apache.log4j.Logger
import scala.util.{Try, Success, Failure}


object SparkKMeansEval {
  val K = 8
  val numRuns = 16
  val maxIters = 200
  val path = "resources/data/chap12/CSCO.csv"
  val logger = Logger.getLogger("SparkKMeansEval")
  
  def run: Int = {
  	Try {
  	   val input = extract
	   val volatilityVol = input(0).zip(input(1)).map( x => Array[Double](x._1, x._2))
		   
	   val config = new SparkKMeansConfig(K, maxIters, numRuns)
	   implicit val sc = new SparkContext("Local", "SparkKMeans")  // no need to load additional jar file
		   
	   val rddConfig = RDDConfig(true, StorageLevel.MEMORY_ONLY)
	   val sparkKMeans = SparkKMeans(config, rddConfig, XTSeries[DblVector](volatilityVol))
		   
	   val obs = Array[Double](0.23, 0.67)
       val clusterId1 = sparkKMeans |> obs
	   Display.show(s"(${obs(0)},${obs(1)}) = $clusterId1", logger)
		   
	   val obs2 = Array[Double](0.56, 0.11)
	   val clusterId2 = sparkKMeans |> obs2 
	   Display.show(s"(${obs2(0)},${obs2(1)}) =  $clusterId2", logger)
	 }
	 match {
	   case Success(n) => n
	   case Failure(e) => Display.error("SparkKMeansEval.run", logger, e)
	 }
  }
  
  private def extract: List[DblVector] = {
      import org.scalaml.trading.YahooFinancials
      import org.scalaml.workflow.data.DataSource
        
      val extractors = List[Array[String] => Double](
      	 YahooFinancials.volatility, YahooFinancials.volume )	

	  DataSource("resources/data/chap12/CSCO.csv", true) |> extractors
   }
}


// ---------------------------------  EOF -------------------------