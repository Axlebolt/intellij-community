// "Replace Stream API chain with loop" "true"

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
  private static List<?>[] test(int[] numbers) {
      List<List<Integer>> list = new ArrayList<>();
      for (int number : numbers) {
          Integer n = number;
          List<Integer> integers = Collections.singletonList(n);
          list.add(integers);
      }
      return list.toArray(new List<?>[0]);
  }

  public static void main(String[] args) {
    System.out.println(Arrays.asList(test(new int[] {1,2,3})));
  }
}