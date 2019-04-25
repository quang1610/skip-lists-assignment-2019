import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The initial height of the skip list.
   */
  static final int INITIAL_HEIGHT = 16;

  // +---------------+-----------------------------------------------
  // | Static Fields |
  // +---------------+

  static Random rand = new Random();

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Pointers to all the front elements.
   */
  public ArrayList<SLNode<K, V>> front() {
    return dummy.next;
  }

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current real height of the skiplist (aka the height of the highest level node).
   */
  int height;

  /**
   * The probability used to determine the height of nodes.
   */
  double prob = 0.5;

  SLNode<K, V> dummy;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new skip list that orders values using the specified comparator.
   */
  public SkipList(Comparator<K> comparator) {
    this.dummy = new SLNode<K, V>(null, null, INITIAL_HEIGHT);
    for (int i = 0; i < INITIAL_HEIGHT; i++) {
      this.dummy.next.set(i, null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = 0;
  } // SkipList(Comparator<K>)

  /**
   * Create a new skip list that orders values using a not-very-clever default comparator.
   */
  public SkipList() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SkipList()

  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  /*
   * (non-Javadoc)
   * 
   * @see SimpleMap#set(java.lang.Object, java.lang.Object) this function set the key and value into
   * the Skip List. If the key already exists in the list, we simply update that node's value.
   */
  @Override
  @SuppressWarnings("unchecked")
  public V set(K key, V value) {
    // if the key is null, throw NullPointerException
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    /*
     * if this is an empty list or the new node need to be add in the front of the list (right after
     * dummy node)
     * 
     * else add new node normally.
     */
    if (this.dummy.next(0) == null || comeBefore(key, this.dummy.next(0).key)) {
      SLNode<K, V> newNode = new SLNode<K, V>(key, value, randomHeight());
      for (int i = 0; i < newNode.getHeight(); i++) {
        newNode.setNext(i, dummy.next(i));
        dummy.setNext(i, newNode);
      }
      // update current height (the height of highest node) and size of the list.
      this.height = Math.max(this.height, newNode.getHeight());
      this.size++;
      return null;
    } else {
      // add new node normally

      // updatePointers holds the pointer to the nodes that will point to new node.
      ArrayList<SLNode<K, V>> updatePointers = (ArrayList<SLNode<K, V>>) this.dummy.next.clone();
      SLNode<K, V> temp = this.dummy;

      // Iterate through each level to find the right place to put new node...
      for (int currentLevel = this.height - 1; currentLevel >= 0; currentLevel--) {
        while (temp != null && temp.next(currentLevel) != null
            && comeBefore(temp.next(currentLevel).key, key)) {
          temp = temp.next(currentLevel);
        }

        // if we found the key already exists in the list, we update the value of that node and exit
        // early!!
        if (temp.next(currentLevel) != null && key.equals(temp.next(currentLevel).key)) {
          V returnValue = temp.next(currentLevel).value;
          temp.next(currentLevel).value = value;
          return returnValue;
        } else {
          // if we haven't found the node with key = input key, we change level, we add temp to
          // update Pointer
          updatePointers.set(currentLevel, temp);
        }
      } // for loop, exit when currentLevel < 0 (aka it reach the 'level 0 of the list')

      // adding new node ...
      SLNode<K, V> newNode = new SLNode<K, V>(key, value, randomHeight());

      // updating size of the list and the height (aka the height of the highest node) of the list.
      this.size++;
      this.height = Math.max(newNode.getHeight(), this.height);

      // Wire old nodes with new node
      for (int i = 0; i < newNode.getHeight(); i++) {
        if (updatePointers.get(i) == null) {
          dummy.setNext(i, newNode);
        } else {
          newNode.setNext(i, updatePointers.get(i).next(i));
          updatePointers.get(i).setNext(i, newNode);
        }
      }
      return null;
    }

  } // set(K,V)

  /*
   * (non-Javadoc)
   * 
   * @see SimpleMap#get(java.lang.Object)
   * 
   * this method return value of the node with key = input key. Else throw IndexOutOfBoundsException
   */
  @Override
  public V get(K key) {
    // if the key is null, throw NullPointerException
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    // if this is an empty list, throw exception.
    if (this.height == 0) {
      throw new IndexOutOfBoundsException("The key was not found.");
    }

    // else iterate through each levels of the list to find the node with key = input key, return
    // immediately if found one.
    SLNode<K, V> temp = this.dummy;
    for (int currentLevel = this.height - 1; currentLevel >= 0; currentLevel--) {
      while (temp.next(currentLevel) != null && comeBefore(temp.next(currentLevel).key, key)) {
        temp = temp.next(currentLevel);
      }

      if (temp.next(currentLevel) != null && temp.next(currentLevel).key.equals(key)) {
        return temp.next(currentLevel).value;
      }
    }

    // if you get to here, the key you are looking for is not in the list.
    throw new IndexOutOfBoundsException("The key was not found.");
  } // get(K,V)

  @Override
  public int size() {
    return this.size;
  } // size()

  @Override
  public boolean containsKey(K key) {
    try {
      get(key);
      return true;
    } catch (Exception e) {
      return false;
    }
  } // containsKey(K)

  /*
   * (non-Javadoc)
   * 
   * @see SimpleMap#remove(java.lang.Object) This method remove the node associated with key, return
   * the value of delete note. If the key is not in the list, return null.
   */
  @SuppressWarnings("unchecked")
  @Override
  public V remove(K key) {
    // if the key is null, throw NullPointerException
    if (key == null) {
      throw new NullPointerException("null key");
    }

    // if the list is empty, return null
    if (dummy.next(0) == null) {
      return null;
    }

    // iterate through the list to find the node to delete
    // updatePointers holds the pointer to the nodes that needed update their 'next' after we remove
    // a node.
    ArrayList<SLNode<K, V>> updatePointers = (ArrayList<SLNode<K, V>>) this.dummy.next.clone();
    SLNode<K, V> temp = this.dummy;
    for (int currentLevel = this.height - 1; currentLevel >= 0; currentLevel--) {
      while (temp != null && temp.next(currentLevel) != null
          && comeBefore(temp.next(currentLevel).key, key)) {
        temp = temp.next(currentLevel);
      }
      updatePointers.set(currentLevel, temp);
    } // for loop. We must keep going till level 0.

    // if there are no node with key in the list, return null
    if (temp.next(0) == null || comeBefore(key, temp.next(0).key)) {
      return null;
    } else {
      // wire nodes before and after the deleted node, update size, update height
      SLNode<K, V> toDelete = temp.next(0);
      this.size--;
      // save the height of the deleted node before we delete that node.
      int oldHeight = temp.next(0).getHeight();
      for (int i = 0; i < oldHeight; i++) {
        // wire things together
        if (updatePointers.get(i) == toDelete) {
          // wire dummy to whatever behind deleted node;
          this.dummy.setNext(i, updatePointers.get(i).next(i).next(i));
        } else {
          updatePointers.get(i).setNext(i, updatePointers.get(i).next(i).next(i));
        }
      }

      // update the height if needed (aka the height of the highest node in the list)
      if (oldHeight >= this.height) {
        int newHeight = 0;
        while (newHeight <= INITIAL_HEIGHT && this.dummy.next(newHeight) != null) {
          newHeight++;
        }
        this.height = newHeight;
      }
      return toDelete.value;
    }
  } // remove(K)

  @Override
  public Iterator<K> keys() {
    return new Iterator<K>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public K next() {
        return nit.next().key;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // keys()

  @Override
  public Iterator<V> values() {
    return new Iterator<V>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public V next() {
        return nit.next().value;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // values()

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    Iterator<SLNode<K, V>> nodes = this.nodes();
    SLNode<K, V> current;
    while (nodes.hasNext()) {
      current = nodes.next();
      action.accept(current.key, current.value);
    }

  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString() return a String contains information of this list
   */
  public String toString() {
    String builder = "";
    Iterator<SLNode<K, V>> it = this.nodes();
    while (it.hasNext()) {
      SLNode<K, V> current = it.next();
      builder += ", (" + current.key.toString() + " " + current.value.toString() + ")";
    }
    return builder;
  }

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    String leading = "          ";

    SLNode<K, V> current = this.dummy.next(0);

    // Print some X's at the start
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" X");
    } // for
    pen.println();
    printLinks(pen, leading);

    while (current != null) {
      // Print out the key as a fixed-width field.
      // (There's probably a better way to do this.)
      String str;
      if (current.key == null) {
        str = "<null>";
      } else {
        str = current.key.toString();
      } // if/else
      if (str.length() < leading.length()) {
        pen.print(leading.substring(str.length()) + str);
      } else {
        pen.print(str.substring(0, leading.length()));
      } // if/else

      // Print an indication for the links it has.
      for (int level = 0; level < current.next.size(); level++) {
        pen.print("-*");
      } // for
        // Print an indication for the links it lacks.
      for (int level = current.next.size(); level < this.height; level++) {
        pen.print(" |");
      } // for
      pen.println();
      printLinks(pen, leading);

      current = current.next.get(0);
    } // while

    // Print some O's at the start
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" O");
    } // for
    pen.println();

  } // dump(PrintWriter)

  /**
   * Print some links (for dump).
   */
  void printLinks(PrintWriter pen, String leading) {
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" |");
    } // for
    pen.println();
  } // printLinks

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Pick a random height for a new node.
   */
  int randomHeight() {
    int result = 1;
    while (rand.nextDouble() < prob) {
      result = result + 1;
    }
    return Math.min(result, INITIAL_HEIGHT);
  } // random\Height()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.dummy.next(0);

      @Override
      public boolean hasNext() {
        return this.next != null;
      } // hasNext()

      @Override
      public SLNode<K, V> next() {
        if (this.next == null) {
          throw new IllegalStateException();
        }
        SLNode<K, V> temp = this.next;
        this.next = this.next.next.get(0);
        return temp;
      } // next();
    }; // new Iterator
  }

  private boolean comeBefore(K key1, K key2) {
    return this.comparator.compare(key1, key2) < 0;
  }

  // nodes()
  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

} // class SkipList


/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key.
   */
  K key;

  /**
   * The value.
   */
  V value;

  /**
   * Pointers to the next nodes.
   */
  ArrayList<SLNode<K, V>> next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new node of height n with the specified key and value.
   */
  public SLNode(K key, V value, int n) {
    this.key = key;
    this.value = value;
    this.next = new ArrayList<SLNode<K, V>>(n);
    for (int i = 0; i < n; i++) {
      this.next.add(null);
    } // for
  } // SLNode(K, V, int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+
  /*
   * sort hand to get an element in this.next
   */
  public SLNode<K, V> next(int i) {
    return this.next.get(i);
  }

  /*
   * short cut to set this.next
   */
  public void setNext(int i, SLNode<K, V> newNode) {
    this.next.set(i, newNode);
  }

  /*
   * return the size of next of this node (also know as this node's height)
   */
  public int getHeight() {
    return this.next.size();
  }
} // SLNode<K,V>
