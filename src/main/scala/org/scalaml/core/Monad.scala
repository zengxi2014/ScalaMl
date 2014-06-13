/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the only purpose of illustrating the 
 * concepts and algorithms presented in Scala for Machine Learning.
 */
package org.scalaml.core


	/**
	 * <p>Generic definition of a Monad used as a template for creating transforms.</p>
	 * @author Patrick Nicolas
	 * @date December 21, 2013
	 */
trait Monad[M[_]] {
  def apply[T](t: T): M[T]
  def map[T,U](m: M[T])(f: T=>U): M[U]
  def flatMap[T,U](m: M[T])(f: T =>M[U]): M[U] 
}


	/**
	 * <p>Monadic container which implements/adapter the most commonly used
	 * Scala higher order methods.This class should not be used directly as they
	 * do not validate any method argument and internal state..</p>
	 * @param _fct element contained and managed by the monadic class
	 * @author Patrick Nicolas
	 * @date December 23, 2013
	 */
import _FCT._
class _FCT[+T](val _fct: T) {
	
  def apply: T = _fct
		/**
		 * Implementation of the map method
		 * @param  c function that converts from type T to type U
		 */
  def map[U](c: T => U): _FCT[U] = new _FCT[U]( c(_fct))
  		/**
		 * Implementation of flatMap
		 * @param  c function that converts from type T to a monadic container of type U
		 */
  def flatMap[U](f: T =>_FCT[U]): _FCT[U] = f(_fct)
    	/**
		 * Implementation of filter for the monadic container
		 * @param  p function that test a condition on the element
		 */
  def filter(p: T =>Boolean): _FCT[T] = if( p(_fct) ) new _FCT[T](_fct) else zeroFCT(_fct)
  		/**
  		 * implementation of the reduce method
  		 * @param f reducer/aggregator/accumulator function applied to the element
  		 */
  def reduceLeft[U](f: (U,T) => U)(implicit c: T=> U): U = f(c(_fct), _fct)
    	/**
  		 * implementation of fold
  		 * @param f reducer/aggregator/accumulator function applied to the element
  		 */
  def foldLeft[U](zero: U)(f: (U, T) => U)(implicit c: T=> U): U =  f(c(_fct), _fct)
  		/**
  		 * implementation of the foreach loop
  		 * @param p immutable method that process the element of the monadic container
  		 */
  def foreach(p: T => Unit): Unit = p(_fct)
}



	/**
	 * Companion object for _FCT that define the constructor apply and the zero
	 * value.
	 */
object _FCT {
  def zeroFCT[T](fct: T): _FCT[T] = new _FCT[T](fct)
		 /**
		  * Generic constructor for the container class.
		  * @param item wrapped in the monadic container
		  * @return an instance of the the monadic container.
		  */
  def apply[T](t: T): _FCT[T] = new _FCT[T](t)
}



// -------------------------------  EOF -----------------------------------