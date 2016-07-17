package epi.solutions;

import epi.solutions.helper.CloneableInputsMap;
import epi.solutions.helper.TimeTests;
import org.junit.Assert;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by psingh on 5/28/16.
 * Problem 7.1
 *
 * @summary Interconvert strings and integers.
 *
 * @problem
 * Take a string representing an integer and return the corresponding integer, and vice versa.
 * Properly handle negative integers.
 */
public class InterconvertingStringInteger {

  private static final int NUM_TESTS = (int) Math.pow(10, 6);
  private static final int INTEGER_STRING_LEN = 8;

  private static String intToString(int x) {
    boolean isNeg = false;
    if (x < 0) {
      isNeg = true;
      x = -x;
    }
    StringBuilder s = new StringBuilder();
    do {
      s.append((char)('0' + x % 10));
      x /= 10;
    } while (x != 0);
    if (isNeg) s.append('-');
    return s.reverse().toString();
  }

  private static int stringToInt(String s) {
    int x = 0;
    boolean isNeg = s.charAt(0) == '-';
    for (int i = (s.charAt(0) == '-') ? 1 : 0; i < s.length(); ++i) {
      int digit = s.charAt(i) - '0';
      x = x*10 + digit;
    }
    return isNeg ? -x : x;
  }

  private static void intToStringTest() throws Exception {
    Callable<CloneableInputsMap> formInputs = () -> {
      CloneableInputsMap inputs = new CloneableInputsMap();
      Random rgen = new Random();
      inputs.addInteger("x", rgen.nextInt());
      return inputs;
    };
    Function<CloneableInputsMap, String> runAlg = (inputs) ->
            intToString(inputs.getInteger("x"));
    Function<CloneableInputsMap, String> knownOutput = (inputs) ->
      String.valueOf(inputs.getInteger("x"));
    Supplier<String> emptyOutput = String::new;
    BiFunction<String, String, Boolean> checkAns = String::equals;
    TimeTests<String> algTimer = new TimeTests<>(formInputs, runAlg, emptyOutput, "IntegerToString");
    algTimer.timeAndCheck(NUM_TESTS, checkAns, knownOutput);
  }

  private static void stringToIntTest() throws Exception {
    Callable<CloneableInputsMap> formInputs = () -> {
      CloneableInputsMap inputs = new CloneableInputsMap();
      Random rgen = new Random();
      StringBuilder s = new StringBuilder();
      if (rgen.nextBoolean()) s.append('-');
      s.append((char)('1' + rgen.nextInt(9)));
      for (int i = 0; i < INTEGER_STRING_LEN - 1; ++i) {
        s.append((char)('0' + rgen.nextInt(10)));
      }
      String str = s.toString();
      Assert.assertTrue(String.format("String \"%s\" represents too big an integer.", str)
              , Integer.MIN_VALUE <= Long.parseLong(str) && Long.parseLong(str) <= Integer.MAX_VALUE);
      inputs.addString("s", str);
      return inputs;
    };
    Function<CloneableInputsMap, Integer> runAlg = (inputs) ->
            stringToInt(inputs.getString("s"));
    Function<CloneableInputsMap, Integer> knownOutput = (inputs) ->
            Integer.parseInt(inputs.getString("s"));
    Supplier<Integer> emptyOutput = () -> 0;
    BiFunction<Integer, Integer, Boolean> checkAns = Integer::equals;
    TimeTests<Integer> algTimer = new TimeTests<>(formInputs, runAlg, emptyOutput, "StringToInteger");
    algTimer.timeAndCheck(NUM_TESTS, checkAns, knownOutput);
  }

  public  static void main(String[] args) throws Exception {
    intToStringTest();
    stringToIntTest();
  }
}
