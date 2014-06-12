/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the only purpose of illustrating the 
 * concepts and algorithms presented in Scala for Machine Learning.
 */
package org.scalaml.app.chap12


import scala.collection.mutable.ArrayBuffer
import org.scalaml.core.Types.ScalaMl._
import java.io.{IOException, PrintWriter}
import akka.actor._
import scala.util.Random
import org.scalaml.stats.Stats



		/**
		 * <p>Class to normalize cross-validation groups. Normalized groups are created
		 * from a generic data set, broken down into N groups or segments then permuting or shuffling data
		 * between these segments in such a way that the variance between the groups or segments is minimal/</p>
		 * @param workers List of reference to the worker or slave actors.
		 * @param data Data set to be processed by the worker actors
		 * @exception IllegalArgumenException if the number of iterations or the number of workers is out of range
		 * 
		 * @author Patrick Nicolas
		 * @date March 23, 2014
		 * @project Scala for Machine Learning
		 */
protected class GroupsNormalizer(val numGroups: Int, val data: XYTSeries) {  
   final val BANDWIDTH = 35
      
   private var workersCnt = numGroups
   private val variances = new DblVector(numGroups)
   private var lowestVariance = Double.MaxValue
   private var bestDistribution: Array[Array[(XY, Int)]] = null
   
   		/**
   		 * Definition of the folds created by broken down the original data set
   		 * into folds or segments of similar size, each of which is processed
   		 * by a dedicated worker actor.
   		 */
   val groups = {
  	  val _groups = new Array[Array[(XY, Int)]](numGroups)
  	  val indexedData = data.zipWithIndex
  	  val grpSize: Int = ((data.size).toDouble/numGroups).floor.toInt
  	    
  	  Range(0, numGroups-1).foreach( n => {
  	     val lowBound = grpSize*n
  	     _groups(n) = indexedData.slice(lowBound, grpSize+lowBound) 
  	  })
  	  _groups(numGroups-1) = indexedData.drop(grpSize*(numGroups-1))
  	  _groups
   }

   		/**
   		 * <p>Retrieve the best combination of groups to perform cross validation.
   		 * The folds are balanced by minimizing the variance between data sets
   		 * @return tuple of array of folds and variance associated with the fold
   		 */
   def normalizedGroups: (Array[Array[(XY, Int)]], Double) = (bestDistribution, lowestVariance)
   
   
		 /**
		  * <p>Update composition of each group and keep track of the most
		  * balanced folds. Balancing the groups to minimize variance is accomplished
		  * by shuffling randomly data between groups.</p>
		  * @param id id of the iteration
		  * @param variance variance related to a specific group
		  * @return true if variance for all the groups has been computed (processed by
		  * all the worker actors.
		  */
   def update(id: Int, variance: Double): Boolean = { 
  	  if( id > 0)
  	  	shuffle(id)
  	  	
  	  workersCnt -= 1
  	  val allWorkersCompleted = workersCnt == 0
  	  variances(workersCnt) = variance
  	  
  	  if( allWorkersCompleted ) {
  	     val lastVariance = evaluateVariance
  	     println("Iteration: " + (id +1) + " cross validator variance: " + lastVariance)
  	     if( lastVariance < lowestVariance) {
  	      	lowestVariance = lastVariance
  	        bestDistribution = groups.clone	
  	     }
  	     workersCnt = numGroups
  	  }
  	  allWorkersCompleted
   }
   
   
   private def shuffle(index: Int): Unit = {
       val lowBand = index*BANDWIDTH % data.size
       val highBand = (lowBand + BANDWIDTH) % data.size
       
       Range(lowBand, highBand).foreach ( n => {
  	  	 val fromGroupIdx = (n+1)%numGroups
  	  	 val toGroupIdx = n%numGroups
  	  	 val fromDataIdx = n % groups(fromGroupIdx).size
  	  	 val toDataIdx = n % groups(toGroupIdx).size
  	  	 
  	  	 groups(fromGroupIdx)(fromGroupIdx) = groups(toGroupIdx)(toDataIdx) 
  	 })
   }
   
   private def evaluateVariance: Double =  Stats[Double](variances).variance
}


// ---------------------------------  EOF -------------------------