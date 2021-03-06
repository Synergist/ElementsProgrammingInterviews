package epi.solutions;

import epi.solutions.helper.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by psingh on 5/25/16.
 * Problem 6.9
 */
public class MaxDifferenceKPairs {
  private static final int ARR_LEN = 20;
  private static final int K_BUY_SELLS = 5;
  private static final int NUM_TESTS = (int) Math.pow(10, 4);
  // Let (B_i)^j = most money you can have after making the jth Buy transaction at A[i]
  // Let (S_i)^j = most money you can have after making the jth Sell transaction at A[i]
  // Then we have the following recurrence:
  //    (B_i)^j = -A[i] + max{(S_p)^(j-1)} for all p < i
  //    (S_i)^j = A[i] + max{(B_p)^j} for all p < i
  //
  // We want S^k = max_i {(S_i)^k}.
  // From the above recurrences, as we iterate i from 0 to n-1, we need only keep track of:
  //    B^j \def= max_[p < i] {(B_p)^j}     and
  //    S^j \def= max_[p < i] {(S_p)^j}
  // Let max_profits = [B^1 S^1 ... B^k S^k]
  // We update max_profits as we iterate over i from 0 to n-1, setting
  //    B^j = max{ B^j , (B_i)^j }
  //    S^j = max{ S^j , (S_i)^j }
  // for j = 1, ..., k for each iteration of i.

  // O(kn) time
  // O(k) space
  private static double maxKPairsProfits(List<Double> A, int k) {
    List<Double> max_profits = new ArrayList<>();
    for (int i = 0; i < k*2; ++i) {
      max_profits.add(Double.NEGATIVE_INFINITY);
    }
    for(int i=0; i < A.size(); ++i) {
      List<Double> old_max_profits = new ArrayList<>(max_profits);
      for (int j=0, sign=-1; j <= i && j < max_profits.size(); ++j, sign *= -1) {
        double diff = sign*A.get(i) + (j == 0 ? 0 : old_max_profits.get(j-1));
        max_profits.set(j, Math.max(diff, old_max_profits.get(j)));
      }
    }
    return max_profits.get(max_profits.size() - 1);
  }

  // O(n^k) checking answer
  private static double checkAnsHelper(List<Double> A, int l, int k, int pre, double ans, double maxAns) {
    double finalMaxAns;
    if (l == k)
      finalMaxAns = Math.max(maxAns, ans);
    else {
      finalMaxAns = maxAns;
      for (int i = pre; i < A.size(); ++i) {
        finalMaxAns = checkAnsHelper(A, l+1, k, i+1, ans + (((l & 1) == 1) ? A.get(i) : -A.get(i)), finalMaxAns);
      }
    }
    return finalMaxAns;
  }

  private static double checkAns(List<Double> A, int k) {
    double ans = 0, maxAns = Double.NEGATIVE_INFINITY;
    maxAns = checkAnsHelper(A, 0, 2*k, 0, ans, maxAns);
    return maxAns;
  }

  // Simpler variant of problem: unlimited # of buy-sell transactions
  // O(n) time
  // O(1) space
  private static double maxProfitUnlimitedPairs(List<Double> A) {
    double profit = 0;
    for (int i = 1; i < A.size(); ++i) {
      double delta = A.get(i) - A.get(i-1);
      if (delta > 0)
        profit += delta;
    }
    return profit;
  }

  public static void main(String[] args) throws Exception {
    Supplier<CloneableInputsMap> formInputs = () -> {
      ArrayList<Double> A = MiscHelperMethods.randNumberArray(Double.class, ARR_LEN);
      CloneableInputsMap inputs = new CloneableInputsMap();
      inputs.addArrayList("A", A);
      return inputs;
    };
    Function<CloneableInputsMap, Double> runAlg = (inputs) -> maxKPairsProfits(inputs.getArrayList("A", Double.class), K_BUY_SELLS);
    Function<CloneableInputsMap, Double> runSimpleAlg = (inputs) -> maxProfitUnlimitedPairs(inputs.getArrayList("A", Double.class));
    Function<CloneableInputsMap, Double> getKnownOutput = (inputs) -> checkAns(inputs.getArrayList("A", Double.class), K_BUY_SELLS);
    AlgVerifierInterfaces< Double, CloneableInputsMap> algVerifier = new OutputComparisonVerifier<>(Double::equals);
    AlgorithmFactory algorithmFactory = new AlgorithmRunnerAndVerifier<>("MaxProfit for k pairs of Buy-Sell transactions", NUM_TESTS, formInputs, runAlg, getKnownOutput, algVerifier);
    AlgorithmFactory simpleAlgorithmFactory = new AlgorithmRunnerAndVerifier<>("MaxProfit for unlimited # of Buy-Sell transactions", NUM_TESTS, formInputs, runSimpleAlg);

    algorithmFactory.runSkipVerif();
    simpleAlgorithmFactory.run();

    // checking is O(n^k) expensive
    algorithmFactory.setNumTests(10000);
    PrintStream originalStream = MiscHelperMethods.setSystemOutToDummyStream();
    algorithmFactory.run();
    System.setOut(originalStream);
  }
}
