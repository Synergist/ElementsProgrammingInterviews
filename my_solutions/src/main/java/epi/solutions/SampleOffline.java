package epi.solutions;

import com.google.common.base.Preconditions;
import com.sun.istack.internal.NotNull;
import epi.solutions.helper.CloneableTestInputsMap;
import epi.solutions.helper.MiscHelperMethods;
import epi.solutions.helper.TimeTests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by psingh on 5/28/16.
 * Problem 6.16
 */
public class SampleOffline {
  private static final int ARR_LEN = (int) Math.pow(10, 3);
  private static final int K = (int) Math.pow(10, 2);
  private static final int NUM_TESTS = (int) Math.pow(10, 6);

  /**
   *
   * @param A an array whose entries are all distinct
   * @param k an integer between [0, A.size()]
   *
   * Problem: Return a random subset of k elements from A. Put them in A[0:k-1] itself.
   * Strategy: Build random susbet one element at a time.
   * If we're allowed to return the result in A itself, then O(1) space.
   * Note: When k > n/2, we can optimize randomly omitting n-k elements instead.
   */
  private static <T> void sampleOffline(@NotNull List<T> A, @NotNull int k) {
    Preconditions.checkArgument(0 <= k && k <= A.size(), "Input k is not within range of [0, A.size()].");
    Random rgen = new Random();
    // Loop invariant: Every permutation of size i is equally likely to be in A[0:i]
    for (int i = 0; i < k; ++i) {
      Collections.swap(A, i, i + rgen.nextInt(A.size() - i));
    }
  }

  public static void main(String[] args) throws Exception {
    Callable<CloneableTestInputsMap> formInput = () -> {
      CloneableTestInputsMap inputs = new CloneableTestInputsMap();
      Random rgen = new Random();
      inputs.addArrayList("A", MiscHelperMethods.randArray(rgen::nextInt, ARR_LEN));
      inputs.addInteger("k", K);
      return inputs;
    };
    Function<CloneableTestInputsMap, ArrayList<Integer>> runAlg = (inputs) -> {
      ArrayList<Integer> A = inputs.getArrayList("A");
      int k = inputs.getInteger("k");
      sampleOffline(A, k);
      return A;
    };
    Supplier<ArrayList<Integer>> emptyOutput = ArrayList::new;
    TimeTests<ArrayList<Integer>> algTimer = new TimeTests<>(formInput, runAlg, emptyOutput, "SampleOffline (permute a random k element subset to front of A)");
    System.out.println(String.format("Running algorithm to select %d random elements from array of length %d...", K, ARR_LEN));
    algTimer.time(NUM_TESTS);
  }
}
