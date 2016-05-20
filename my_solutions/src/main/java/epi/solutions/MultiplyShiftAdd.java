package epi.solutions;

//import epi.solutions.helper.TimeTests;

import epi.solutions.helper.CloneableInteger;
import epi.solutions.helper.CloneableTestInputsMap;
import epi.solutions.helper.TimeTests;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by psingh on 5/12/16.
 * Problem 5.5
 */
public class MultiplyShiftAdd {
  private static final int NUM_TESTS = (int) Math.pow(10, 6);
  private static final int MAX_INT_INPUT = (1 << 16) - 1;
  private static long multiply(int x, int y) {
    long sum = 0;
    while (x != 0) {
      // Examine each bit of x
      if ((x & 1) != 0)
        sum = add(sum, y);
      x >>>= 1;
      y <<= 1;
    }
    return sum;
  }

  private static long add(long a, long b) {
    long sum = 0, carryin = 0, carryout = 0, k=1, tempA = a, tempB = b;
    while (tempA != 0 || tempB != 0) {
      long ak = a & k, bk = b & k;
      carryout = ((ak & bk) | (ak & carryin) | (bk & carryin)) << 1;
      sum |= (ak ^ bk ^ carryin); // ^
      carryin = carryout;
      k <<= 1;
      tempA >>>= 1;
      tempB >>>= 1;
    }
    return sum | carryin;
  }

  public static void main(String[] args) {
    if (args.length == 2) {
      int x = Integer.parseInt(args[0]), y = Integer.parseInt(args[1]);
      long res = multiply(x,y);
      assert(res == (long)x*y);
    } else {
      Callable<CloneableTestInputsMap> formInput = () -> {
        Random rgen = new Random();
        CloneableTestInputsMap inputs = new CloneableTestInputsMap();
        inputs.put("x", new CloneableInteger(rgen.nextInt(MAX_INT_INPUT)));
        inputs.put("y", new CloneableInteger(rgen.nextInt(MAX_INT_INPUT)));
        return inputs;
      };
      Function<CloneableTestInputsMap, Long> runAlg = (inputs) ->
              multiply(((CloneableInteger) inputs.get("x")).data
                      , ((CloneableInteger) inputs.get("y")).data);
      Function<CloneableTestInputsMap, Long> getKnownOutput = (orig_inputs) ->
              ((long) ((CloneableInteger) orig_inputs.get("x")).data) *
              ((CloneableInteger) orig_inputs.get("y")).data;
      BiFunction<Long, Long, Boolean> checkResults = Long::equals;
      Supplier<Long> emptyOutput = () -> 0L;
      TimeTests<Long> algTimer =
              new TimeTests<>(formInput, runAlg, emptyOutput, "MultiplyShiftAdd");
      algTimer.testAndCheck(NUM_TESTS, checkResults, getKnownOutput);
    }
  }
}
