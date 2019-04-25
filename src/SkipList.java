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
  ArrayList<SLNode<K, V>> front;

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current height of the skiplist.
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
    this.front = new ArrayList<SLNode<K, V>>(INITIAL_HEIGHT);
    this.dummy = new SLNode<K, V>(null, null, INITIAL_HEIGHT);
    for (int i = 0; i < INITIAL_HEIGHT; i++) {
      front.add(null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = INITIAL_HEIGHT;
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

  @SuppressWarnings("unchecked")
  @Override
  public V set(K key, V value) {
    if (key == null) {
      throw new NullPointerException("null key");
    }

    /*
     * 3 cases:
     * 
     * if current list is null
     * 
     * if the node we set is at the beginning of the list
     * 
     * if the node we set is in the middle or end of the list.
     */

    if (this.front.get(0) == null) {

      // add new node to the begin of an empty list

      SLNode<K, V> newNode = new SLNode<K, V>(key, value, randomHeight());
      for (int i = 0; i < newNode.getHeight(); i++) {
        this.dummy.next.set(i, newNode);
      }
    } else if (this.comparator.compare(this.front.get(0).key, key) > 0) {

      // add new node to the beginning of a not empty list and wire new node with nodes behind

      SLNode<K, V> newNode = new SLNode<K, V>(key, value, randomHeight());
      for (int i = 0; i < newNode.getHeight(); i++) {
        newNode.next.set(i, this.dummy.next.get(i));
        this.dummy.next.set(i, newNode);
      }
    } else {

      // Big case, insert

      ArrayList<SLNode<K, V>> tempNodeLst = (ArrayList<SLNode<K, V>>) this.front.clone();
      SLNode<K, V> tempNode = this.dummy.next.get(this.getRealHeight() - 1);

      // loop through the list from the top to bottom level, find the place to insert the new node.
      // If key already exist inside the list, update the node in the list.
      for (int currentHeight = this.height - 1; currentHeight >= 0; currentHeight--) { // for each
                                                                                       // level...
        // if the key of the tempNode < key, we forward it
        while (this.comparator.compare(tempNode.key, key) < 0) {
          tempNode = tempNode.next.get(currentHeight);
        }
        if (this.comparator.compare(tempNode.key, key) == 0) {
          V returnValue = tempNode.value;
          tempNode.value = value;
          return returnValue;
        }

      }
    }
    return null;

  } // set(K,V)

  @Override
  public V get(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if
    SLNode<K, V> current = front.get(this.height - 1);
    for (int currHeight = this.height - 1; currHeight >= 0; currHeight--) {
      while (current.next.get(currHeight) != null
          && this.comparator.compare(key, current.next.get(currHeight).key) < 0) {
        current = current.next.get(currHeight);
      }
      if (current.next.get(currHeight).key.equals(key)) {
        return current.next.get(currHeight).value;
      }
    }
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

  @SuppressWarnings("unchecked")
  @Override
  public V remove(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    }

    if (this.front.get(0) == null) {
      return null;
    }

    ArrayList<SLNode<K, V>> nodeList = (ArrayList<SLNode<K, V>>) front.clone();
    SLNode<K, V> current = null;
    for (int currHeight = this.height - 1; currHeight >= 0; currHeight--) {
      current = nodeList.get(currHeight);
      while (current.next.get(currHeight) != null
          && this.comparator.compare(key, current.next.get(currHeight).key) < 0) {
        current = current.next.get(currHeight);
      }
      nodeList.set(currHeight, current);
    }

    if (current.next.get(0) == null || this.comparator.compare(current.next.get(0).key, key) > 0) {
      return null;
    } else {
      V toReturn = null;
      for (int i = 0; i < this.height; i++) {
        if (nodeList.get(i) != null) {
          if (nodeList.get(i).key.equals(key)) {
            toReturn = nodeList.get(i).value;
            this.front.set(i, nodeList.get(i).next.get(i));
          } else {
            toReturn = nodeList.get(i).next.get(i).value;
            nodeList.get(i).next.set(i, nodeList.get(i).next.get(i).next.get(i));
          }
        }
      }
      return toReturn;
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

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    String leading = "          ";

    SLNode<K, V> current = front.get(0);

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

  private int getRealHeight() {
    int counter = 0;
    while (this.front.get(counter) != null) {
      counter++;
      if (counter == this.INITIAL_HEIGHT) {
        return counter;
      }
    }
    return counter;
  }
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
    return Math.min(result, this.height);
  } // random\Height()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.front.get(0);

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
  } // nodes()

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

  public int getHeight() {
    return this.next.size();
  }
} // SLNode<K,V>
