package epi.solutions;

import epi.solutions.helper.AlgorithmFactory;
import epi.solutions.helper.AlgorithmRunnerAndVerifier;
import epi.solutions.helper.CloneableInputsMap;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by psingh on 5/20/16.
 * Problem 5.4
 * Define the weight of a nonnegative integer to be the number of bits
 * that are set to 1 in its binary representation.
 */
public class ClosestIntSameBits {
  // We restrict our attention to 63 LSBs
  private static final int NUM_UNSIGN_BITS = 63;
  private static final int NUM_TESTS = (int) Math.pow(10, 6);

/**
 * @param x A nonnegative integer x (whose binary representation is not all 0s or 1s)
 * @return  A nonnegative integer y \neq x, s.t. y has the same weight as x
 *          and the difference of x and y is as small as possible.
 */
  private static long closestIntSameBitCount(long x) {
    for (int i = 0; i < NUM_UNSIGN_BITS - 1; ++i) {
      if(((x >>> i) & 1) != ((x >>> (i + 1)) & 1)) {
        x ^= (1L << i) | (1L << (i+1));
        return x;
      }
    }

    throw new IllegalArgumentException("All bits are 0 or 1");
  }

  private static void smallTest() {
    assert(closestIntSameBitCount(6) == 5);
    assert(closestIntSameBitCount(7) == 11);
    assert(closestIntSameBitCount(2) == 1);
    assert(closestIntSameBitCount(32) == 16);
    assert(closestIntSameBitCount(Long.MAX_VALUE - 1) == Long.MAX_VALUE - 2);
  }

  public static void main(String[] args) throws Exception {
    smallTest();
    Supplier<CloneableInputsMap> formInputs = () -> {
      Random rgen = new Random();
      CloneableInputsMap inputs = new CloneableInputsMap();
      inputs.addLong("x", rgen.nextLong());
      return inputs;
    };
    Function<CloneableInputsMap, Long> runAlg = (inputs) ->
            closestIntSameBitCount(inputs.getLong("x"));

    AlgorithmFactory algorithmFactory = new AlgorithmRunnerAndVerifier<>("ClosestIntSameBits", NUM_TESTS, formInputs, runAlg);
    algorithmFactory.run();
  }

}
