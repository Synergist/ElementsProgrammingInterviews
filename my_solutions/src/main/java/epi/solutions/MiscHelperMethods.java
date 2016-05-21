package epi.solutions;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Created by psingh on 5/20/16.
 */
public class MiscHelperMethods {

  public static <T> ArrayList<T> randArray(Supplier<T> randSupplier, int len) {
    ArrayList<T> result = new ArrayList<>(len);
    for (int i = 0; i < len; ++i) {
      result.add(randSupplier.get());
    }
    return result;
  }
}
