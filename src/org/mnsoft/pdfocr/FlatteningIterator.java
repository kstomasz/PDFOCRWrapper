package org.mnsoft.pdfocr;

import java.lang.reflect.Array;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;


/**
 * An iterator that 'flattens out' collections, iterators, arrays, etc.
 *
 * That is it will iterate out their contents in order, descending into any
 * iterators, iterables or arrays provided to it.
 *
 * An example (not valid Java for brevity - some type declarations are ommitted):
 *
 * new FlattingIterator({1, 2, 3}, {{1, 2}, {3}}, new ArrayList({1, 2, 3}))
 *
 * Will iterate through the sequence 1, 2, 3, 1, 2, 3, 1, 2, 3.
 *
 * Note that this implements a non-generic version of the Iterator interface so
 * may be cast appropriately - it's very hard to give this class an appropriate
 * generic type.
 *
 * The code is based on code from David R. MacIver.
 *
 * This program is a free software available under the GNU
 * Lesser General Public License.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author (c) 2010, David R. MacIver; adapted by Matthias Nott (a.k.a. David did all the work, Matthias just abuses it.)
 */
@SuppressWarnings("rawtypes")
public class FlatteningIterator implements Iterator {
  /*
   * Marker object. This is never exposed outside this class, so can be guaranteed
   * to be != anything else. We use it to indicate an absence of any other object.
   */
  private final Object             blank     = new Object();

  /*
   * This stack stores all the iterators found so far. The head of the stack is
   * the iterator which we are currently progressing through
   */
  private final Stack<Iterator<?>> iterators = new Stack<Iterator<?>>();

  /*
   * Storage field for the next element to be returned. blank when the next element
   * is currently unknown.
   */
  private Object                   next      = blank;

  public FlatteningIterator(Object... objects) {
    this.iterators.push(Arrays.asList(objects).iterator());
  }

  public void remove() {
    /* Not implemented */ }


  private void moveToNext() {
    if ((next == blank) && !this.iterators.empty()) {
      if (!iterators.peek().hasNext()) {
        iterators.pop();
        moveToNext();
      } else {
        final Object next = iterators.peek().next();
        if (next instanceof Iterator) {
          iterators.push((Iterator<?>) next);
          moveToNext();
        } else if (next instanceof Iterable) {
          iterators.push(((Iterable) next).iterator());
          moveToNext();
        } else if (next instanceof Array) {
          iterators.push(Arrays.asList((Array) next).iterator());
          moveToNext();
        } else {
          this.next = next;
        }
      }
    }
  }


  /**
   * Returns the next element in our iteration, throwing a NoSuchElementException
   * if none is found.
   */
  public Object next() {
    moveToNext();

    if (this.next == blank) {
      throw (new NoSuchElementException());
    } else {
      Object next = this.next;
      this.next = blank;

      return next;
    }
  }


  /**
   * Returns if there are any objects left to iterate over. This method
   * can change the internal state of the object when it is called, but repeated
   * calls to it will not have any additional side effects.
   */
  public boolean hasNext() {
    moveToNext();

    return (this.next != blank);
  }
}
