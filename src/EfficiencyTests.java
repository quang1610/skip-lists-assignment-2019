import java.util.ArrayList;
import java.util.Random;
import java.io.PrintWriter;
import java.math.BigInteger;


// find more about the result in the Efficiency_Analysis.pdf in the git repo.
public class EfficiencyTests {
  static SkipList<Integer, String> myList = new SkipList<Integer, String>();
  static ArrayList<Integer> keys = new ArrayList<Integer>();
  static PrintWriter pen = new PrintWriter(System.out, true);
  static Random random = new Random();

  public static void main(String[] args) {
    // test size 100
    testEfficiency(100);

    // test size 200
    testEfficiency(200);

    // test size 500
    testEfficiency(500);

    // test size 1000
    testEfficiency(1000);

    // test size 2000
    testEfficiency(2000);

    // test size 5000
    testEfficiency(5000);

    // test size 10000
    testEfficiency(10000);

    // test size 20000
    testEfficiency(20000);

    // test size 50000
    testEfficiency(50000);

    // test size 100000
    testEfficiency(100000);

    // test size 200000
    testEfficiency(200000);

    // test size 500000
    testEfficiency(500000);
  }

  /*
   * return the operation of adding key and value into SkipList
   */
  static long countSet(int key, String value) {
    SkipList.operationCount = 0;
    myList.set(key, value);
    return SkipList.operationCount;
  }

  /*
   * return the operation of getting key from SkipList
   */
  static long countGet(int key) {
    SkipList.operationCount = 0;
    myList.get(key);
    return SkipList.operationCount;
  }

  /*
   * return the operation of removing key from SkipList
   */
  static long countRemove(int key) {
    SkipList.operationCount = 0;
    myList.remove(key);
    return SkipList.operationCount;
  }

  /*
   * initialize a SkipList with size = size;
   */
  static void initialize(int size) {
    keys.clear();
    myList = new SkipList<Integer, String>();
    for (int i = 0; i < size; i++) {
      int num = random.nextInt(Integer.MAX_VALUE);
      myList.set(num, "hello");
      keys.add(num);
    }
  }

  /*
   * run SkipList methods set, get, remove for 100 times on the list and take the average operation
   * count of each method.
   * 
   * we set new key-value in to SkipList and after that we call remove a key exists inside the
   * SkipList. Thus we maintain the size of SkipList after each cycle of loop.
   */
  static void testEfficiency(int size) {
    initialize(size);

    BigInteger getCounter = BigInteger.valueOf(0);
    BigInteger setCounter = BigInteger.valueOf(0);
    BigInteger removeCounter = BigInteger.valueOf(0);

    for (int i = 0; i < 100; i++) {
      getCounter =
          getCounter.add(BigInteger.valueOf(countGet(keys.get(random.nextInt(keys.size())))));

      int num = random.nextInt(Integer.MAX_VALUE);
      keys.add(num);
      setCounter = setCounter.add(BigInteger.valueOf(countSet(num, "hello")));

      int indexKeyToBeDeleted = random.nextInt(keys.size());
      removeCounter =
          removeCounter.add(BigInteger.valueOf(countRemove(keys.get(indexKeyToBeDeleted))));
      keys.remove(indexKeyToBeDeleted);
    }

    getCounter = getCounter.divide(BigInteger.valueOf(100));
    setCounter = setCounter.divide(BigInteger.valueOf(100));
    removeCounter = removeCounter.divide(BigInteger.valueOf(100));

    pen.println("Efficiency test with size of list = " + size);
    pen.println("Get counter = " + getCounter);
    pen.println("Set counter = " + setCounter);
    pen.println("Remove counter = " + removeCounter);
  }
  
  /*
     *Efficiency test with size of list = 100
      Get counter = 50
      Set counter = 63
      Remove counter = 51
      Efficiency test with size of list = 200
      Get counter = 59
      Set counter = 73
      Remove counter = 63
      Efficiency test with size of list = 500
      Get counter = 66
      Set counter = 83
      Remove counter = 65
      Efficiency test with size of list = 1000
      Get counter = 74
      Set counter = 88
      Remove counter = 68
      Efficiency test with size of list = 2000
      Get counter = 90
      Set counter = 106
      Remove counter = 85
      Efficiency test with size of list = 5000
      Get counter = 92
      Set counter = 106
      Remove counter = 83
      Efficiency test with size of list = 10000
      Get counter = 95
      Set counter = 111
      Remove counter = 85
      Efficiency test with size of list = 20000
      Get counter = 106
      Set counter = 120
      Remove counter = 92
      Efficiency test with size of list = 50000
      Get counter = 111
      Set counter = 125
      Remove counter = 98
      Efficiency test with size of list = 100000
      Get counter = 121
      Set counter = 134
      Remove counter = 106
      Efficiency test with size of list = 200000
      Get counter = 128
      Set counter = 141
      Remove counter = 111
      Efficiency test with size of list = 500000
      Get counter = 129
      Set counter = 144
      Remove counter = 117
   */
}


