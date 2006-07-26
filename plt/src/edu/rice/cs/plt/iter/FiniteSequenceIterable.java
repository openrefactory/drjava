package edu.rice.cs.plt.iter;

import edu.rice.cs.plt.lambda.Lambda;

/**
 * An iterable representing a finite sequence.  The sequence is defined by an initial
 * value and a successor function (described by a {@link Lambda}), along with a size that
 * truncates the (otherwise infinite) list.
 */
public class FiniteSequenceIterable<T> extends AbstractIterable<T> implements SizedIterable<T> {
  
  private final T _initial;
  private final Lambda<? super T, ? extends T> _successor;
  private final int _size;
  
  /**
   * @param initial  The first value in the sequence
   * @param successor  A function that, given the nth sequence value, produces the n+1st value
   * @param size  The number of elements in the sequence
   * @throws IllegalArgumentException  If size is less than {@code 0}
   */
  public FiniteSequenceIterable(T initial, Lambda<? super T, ? extends T> successor, int size) {
    if (size < 0) { throw new IllegalArgumentException("size < 0"); }
    _initial = initial;
    _successor = successor;
    _size = size;
  }
  
  /** Create a new {@link FiniteSequenceIterator} based on this iterable's parameters */
  public FiniteSequenceIterator<T> iterator() {
    return new FiniteSequenceIterator<T>(_initial, _successor, _size);
  }
  
  public int size() { return _size; }
  
  public boolean isFixed() { return true; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> FiniteSequenceIterable<T> make(T initial, Lambda<? super T, ? extends T> successor, 
                                                   int size) { 
    return new FiniteSequenceIterable<T>(initial, successor, size);
  }
  
  /**
   * Create a {@code FiniteSequenceIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the sequence.
   */
  public static <T> SnapshotIterable<T> makeSnapshot(T initial, Lambda<? super T, ? extends T> successor, 
                                                     int size) { 
    return new SnapshotIterable<T>(new FiniteSequenceIterable<T>(initial, successor, size));
  }
  
  /** 
   * Create a simple sequence containing the numbers between {@code start} and {@code end}
   * (inclusive).  {@code start} may be less than <em>or</em> greater than {@code end} (or even
   * equal to it); the resulting iterator will increment or decrement as necessary.
   */
  public static FiniteSequenceIterable<Integer> makeIntegerSequence(int start, int end) {
    if (start <= end) {
      return new FiniteSequenceIterable<Integer>(start, Lambda.INCREMENT_INT, end-start+1);
    }
    else {
      return new FiniteSequenceIterable<Integer>(start, Lambda.DECREMENT_INT, start-end+1);
    }
  }
  
}
