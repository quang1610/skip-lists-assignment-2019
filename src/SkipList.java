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
	 * The current real height of the skiplist (aka the height of the highest level
	 * node).
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
	 * Create a new skip list that orders values using a not-very-clever default
	 * comparator.
	 */
	public SkipList() {
		this((k1, k2) -> k1.toString().compareTo(k2.toString()));
	} // SkipList()

	// +-------------------+-------------------------------------------
	// | SimpleMap methods |
	// +-------------------+

	@Override
	@SuppressWarnings("unchecked")
	public V set(K key, V value) {
		if (key == null) {
			throw new NullPointerException("null key");
		} // if

		if (this.dummy.next(0) == null || comeBefore(key, this.dummy.next(0).key)) {
			SLNode<K, V> newNode = new SLNode<K, V>(key, value, randomHeight());
			for (int i = 0; i < newNode.getHeight(); i++) {
				newNode.setNext(i, dummy.next(i));
				dummy.setNext(i, newNode);
			}
			this.height = Math.max(this.height, newNode.getHeight());
			this.size++;
			return null;
		} else {
			ArrayList<SLNode<K, V>> updatePointers = (ArrayList<SLNode<K, V>>) this.dummy.next.clone();
			SLNode<K, V> temp = this.dummy;
			for (int currentLevel = this.height - 1; currentLevel >= 0; currentLevel--) {
				while (temp != null && temp.next(currentLevel) != null
						&& comeBefore(temp.next(currentLevel).key, key)) {
					temp = temp.next(currentLevel);
				}

				// if we found the key already exists in the list
				if (temp.next(currentLevel) != null && key.equals(temp.next(currentLevel).key)) {
					V returnValue = temp.next(currentLevel).value;
					temp.next(currentLevel).value = value;
					return returnValue;
				} else {
					// if we change level, we add temp to update Pointer
					updatePointers.set(currentLevel, temp);
				}
			}

			SLNode<K, V> newNode = new SLNode<K, V>(key, value, randomHeight());
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

	@Override
	public V get(K key) {
		if (key == null) {
			throw new NullPointerException("null key");
		} // if

		if (this.height == 0) {
			throw new IndexOutOfBoundsException("The key was not found.");
		}

		SLNode<K, V> temp = this.dummy;
		for (int currentLevel = this.height - 1; currentLevel >= 0; currentLevel--) {
			while (temp.next(currentLevel) != null && comeBefore(temp.next(currentLevel).key, key)) {
				temp = temp.next(currentLevel);
			}

			if (temp.next(currentLevel) != null && temp.next(currentLevel).key.equals(key)) {
				return temp.next(currentLevel).value;
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
		if (dummy.next(0) == null) {
			return null;
		}
		ArrayList<SLNode<K, V>> updatePointers = (ArrayList<SLNode<K, V>>) this.dummy.next.clone();
		SLNode<K, V> temp = this.dummy;
		for (int currentLevel = this.height - 1; currentLevel >= 0; currentLevel--) {
			while (temp != null && temp.next(currentLevel) != null && comeBefore(temp.next(currentLevel).key, key)) {
				temp = temp.next(currentLevel);
			}
			updatePointers.set(currentLevel, temp);
		}

		// if there are no node with key in the list, return null
		if (temp.next(0) == null || comeBefore(key, temp.next(0).key)) {
			return null;
		} else {
			// wire nodes before and after the deleted node, update size, update height
			SLNode<K,V> toDelete = temp.next(0);
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

			if (oldHeight >= this.height) {
				int newHeight = 0;

				while (newHeight <= INITIAL_HEIGHT && this.dummy.next(newHeight) != null) {
					newHeight++;
				}
				this.height = newHeight;
			}
			return toDelete.value;
		}

		/*
		 * SLNode<K,V> newNode = new SLNode<K,V>(key, value, randomHeight());
		 * this.size++; this.height = Math.max(newNode.getHeight(), this.height);
		 * 
		 * // Wire old nodes with new node for(int i = 0; i < newNode.getHeight(); i++)
		 * { if(updatePointers.get(i) == null) { dummy.setNext(i, newNode); } else {
		 * newNode.setNext(i, updatePointers.get(i).next(i));
		 * updatePointers.get(i).setNext(i, newNode); } }
		 */

		/*
		 * if (key == null) { throw new NullPointerException("null key"); }
		 * 
		 * if (this.front().get(0) == null) { return null; }
		 * 
		 * ArrayList<SLNode<K, V>> nodeList = (ArrayList<SLNode<K, V>>) front().clone();
		 * SLNode<K, V> current = null; for (int currHeight = this.height - 1;
		 * currHeight >= 0; currHeight--) { current = nodeList.get(currHeight); while
		 * (current.next.get(currHeight) != null && this.comparator.compare(key,
		 * current.next.get(currHeight).key) < 0) { current =
		 * current.next.get(currHeight); } nodeList.set(currHeight, current); }
		 * 
		 * if (current.next.get(0) == null ||
		 * this.comparator.compare(current.next.get(0).key, key) > 0) { return null; }
		 * else { V toReturn = null; for (int i = 0; i < this.height; i++) { if
		 * (nodeList.get(i) != null) { if (nodeList.get(i).key.equals(key)) { toReturn =
		 * nodeList.get(i).value; this.front().set(i, nodeList.get(i).next.get(i)); }
		 * else { toReturn = nodeList.get(i).next.get(i).value;
		 * nodeList.get(i).next.set(i, nodeList.get(i).next.get(i).next.get(i)); } } }
		 * return toReturn; }
		 */
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

	public String toString() {
		String builder = "";
		Iterator<SLNode<K, V>> it = this.nodes();
		while (it.hasNext()) {
			SLNode<K, V> current = it.next();
			builder += ", (" + current.key.toString() + " " + current.value.toString() + ")";
		}
		return builder;
	}

	public void dump(PrintWriter pen) {
		String leading = "          ";

		SLNode<K, V> current = front().get(0);

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
	 * Get an iterator for all of the nodes. (Useful for implementing the other
	 * iterators.)
	 */
	Iterator<SLNode<K, V>> nodes() {
		return new Iterator<SLNode<K, V>>() {

			/**
			 * A reference to the next node to return.
			 */
			SLNode<K, V> next = SkipList.this.front().get(0);

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
	public SLNode<K, V> next(int i) {
		return this.next.get(i);
	}

	public void setNext(int i, SLNode<K, V> newNode) {
		this.next.set(i, newNode);
	}

	public int getHeight() {
		return this.next.size();
	}
} // SLNode<K,V>
