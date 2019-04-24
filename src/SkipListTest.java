import java.io.PrintWriter;

public class SkipListTest {

	public static void main(String[] args) {
		SkipList<Integer,String> myList = new SkipList<Integer,String>();
		PrintWriter pen = new PrintWriter(System.out,true);
		pen.print("something");
		pen.println(myList.toString());
		myList.set(6, "six");
		pen.println(myList.toString());
		myList.set(4, "four");
		pen.println(myList.toString());
		myList.set(8, "eight");
		pen.println(myList.toString());
		myList.dump(pen);
		myList.remove(8);
		myList.dump(pen);

	}

}
