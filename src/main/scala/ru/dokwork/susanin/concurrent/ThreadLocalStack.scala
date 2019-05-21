package ru.dokwork.susanin.concurrent

import ru.dokwork.susanin.Span

/**
  * This stack is a unique for every thread. By this reason all operation are thread-safe.
  */
class ThreadLocalStack[E] {
  type InnerStack = List[E]
  private val stack: ThreadLocal[InnerStack] = ThreadLocal.withInitial(() ⇒ List.empty[E])

  /**
    * Returns specified for current thread stack values.
    */
  def get: InnerStack = stack.get()

  /**
    * Change values of the stack for current thread.
    *
    * @param value stack with new values.
    */
  def set(value: InnerStack): Unit = stack.set(value)

  /**
    * Applies specified for current thread stack values to the function `f` and set it result as new
    * values of the stack.
    *
    * @param f the function which calculates new values of the stack.
    */
  def update(f: InnerStack ⇒ InnerStack): Unit = set(f(get))

  /**
    * Set the values of the stack while executing the specified function.
    *
    * @param values the values to which to set the stack.
    * @param f the function to evaluate under the new setting
    */
  def withValues[A](values: InnerStack)(f: ⇒ A): A = {
    val prev = get
    set(values)
    try { f } finally { set(prev) }
  }

  /**
    * Looks at the object at the top of this stack without removing it
    * from the stack.
    *
    * @return some object at the top of this stack, or `None` in case of empty stack.
    */
  def peek(): Option[E] = get.headOption

  /**
    * Remove an item onto the top of this stack.
    *
    * @param   item   the item to be pushed onto this stack.
    * @return  the `item` argument.
    */
  def push(item: E): Unit = set(item :: get)

  /**
    * Removes the object at the top of this stack.
    *
    * @return  some object at the top of this stack or `None` in case of empty stack.
    */
  def pop(): Option[E] = get match {
    case Nil       ⇒ None
    case x :: tail ⇒ set(tail); Some(x)
  }

  /**
    * Returns the number of components in this stack..
    */
  def size: Int = get.size

  /**
    * Returns `true` if this stack has no components; `false` otherwise.
    */
  def isEmpty: Boolean = get.isEmpty
}

object ThreadLocalStack {
  /**
   * Global stack with spans which used in every class from package `concurrent`.
   */
  private[susanin] lazy val globalSpansStack = new ThreadLocalStack[Span]
}