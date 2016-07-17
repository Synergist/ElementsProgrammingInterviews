package epi.solutions.helper;

import com.google.common.base.Preconditions;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class TimeTests<outputType> {
  private Callable<CloneableInputsMap> _formInput;
  private Function<CloneableInputsMap, outputType> _runAlgorithm;
  private Function<CloneableInputsMap, outputType> _getKnownOutput;
  private int _numTests;
  private String _algDescription;
  private Function<AlgCompleteData<outputType>, Boolean> _algChecker;
  private Function<CloneableInputsMap, CloneableInputsMap> _saveExtraAlgResults;
  private Function<CloneableInputsMap, CloneableInputsMap> _saveExtraExpResults;
  private boolean knownOutputSet;
  private boolean extraAlgResultsSaved;
  private boolean extraExpResultsSaved;
  private boolean _parallel;

  public TimeTests(Callable<CloneableInputsMap> formInput
                  , Function<CloneableInputsMap, outputType> runAlgorithm
                  , Supplier<outputType> emptyOutput
                  , String algName) {
    _formInput = formInput;
    _runAlgorithm = runAlgorithm;
    _algDescription = algName;

    _getKnownOutput = (inputs) -> emptyOutput.get();
    _saveExtraAlgResults = (inputs) -> new CloneableInputsMap();
    _saveExtraExpResults = (inputs) -> new CloneableInputsMap();
    _parallel = true;
    knownOutputSet = false;
    extraAlgResultsSaved = false;
    extraExpResultsSaved = false;
    // TODO: prob not a good practice to use Object as value type in HashMap here and use unchecked type casting below. Try to figure out a better approach...
  }

  // See https://stackoverflow.com/questions/1998544/method-has-the-same-erasure-as-another-method-in-type
  // for why classes aren't allowed to have methods that are override-equivalent,
  // i.e. methods that have the same parameter types after erasure
  // It took a bit of brainstorming to overload timeAndCheck method in a valid way as follows
  public void timeAndCheck(final int numTests, Function<outputType, Boolean> checkResults) throws Exception {
    _numTests = numTests;
    _algChecker = algCompleteData -> checkResults.apply(algCompleteData._observedResults);
    timeAndCheck();
  }

  public void timeAndCheck(final int numTests, BiFunction<outputType, outputType, Boolean> checkResults
          , Function<CloneableInputsMap, outputType> getKnownOutput) throws Exception {
    _numTests = numTests;
    _getKnownOutput = getKnownOutput;
    _algChecker = algCompleteData -> checkResults.apply(algCompleteData._observedResults, algCompleteData._expectedResults);
    timeAndCheck();
  }

  public void timeAndCheck(final int numTests, BiFunction<CloneableInputsMap, outputType, Boolean> checkResults) throws Exception {
    _numTests = numTests;
    _algChecker = algCompleteData -> checkResults.apply(algCompleteData._orig_inputs, algCompleteData._observedResults);
    timeAndCheck();
  }

  /* TODO: Add a timeAndCheck() method that allows for running the algorithm multiple times and verifying that the
            distribution of results matches expectations. For example, time and check that SampleOffline.java sends
            approx equal random subsets of k elements to the front of A.
     Notes: it could possibly be worked into timeAndCheck(final int numTests, Function<outputType, Boolean> checkResults) ...?
    */

  // TODO: Clearer variable names and better documentation/explanation of use cases for these functional interfaces.
  @FunctionalInterface
  public interface TriFunction<A, B, C, D> {
    D apply(A a, B b, C c);
  }

  @FunctionalInterface
  public interface QuadFunction<A, B, C, D, E> {
    E apply(A a, B b, C c, D d);
  }

  // TODO: Start using setKnownOutput and setExtraResults instead of this mess of parameters
  public void timeAndCheck(final int numTests, TriFunction<outputType, outputType, CloneableInputsMap, Boolean> checkResults) throws Exception {
    Preconditions.checkState(knownOutputSet, "setKnownOutput has not been called on this TimeTests instance.");
    Preconditions.checkState(extraAlgResultsSaved, "saveExtraAlgResults has not been called on this TimeTests instance.");
    _numTests = numTests;
    _algChecker = algCompleteData ->
            checkResults.apply(algCompleteData._observedResults, algCompleteData._expectedResults, algCompleteData._algExtraResults);
    timeAndCheck();
  }

  public void timeAndCheck(final int numTests, QuadFunction<outputType, outputType, CloneableInputsMap, CloneableInputsMap, Boolean> checkResults) throws Exception {
    Preconditions.checkState(knownOutputSet, "setKnownOutput has not been called on this TimeTests instance.");
    Preconditions.checkState(extraAlgResultsSaved, "saveExtraAlgResults has not been called on this TimeTests instance.");
    Preconditions.checkState(extraExpResultsSaved, "saveExtraExpResults has not been called on this TimeTests instance.");
    _numTests = numTests;
    _algChecker = algCompleteData ->
            checkResults.apply(algCompleteData._observedResults, algCompleteData._expectedResults
                    , algCompleteData._algExtraResults, algCompleteData._expectedExtraResults);
    timeAndCheck();
  }

  public void time(final int numTests) throws Exception {
    _numTests = numTests;
    _algChecker = (completeData) -> true;
    timeAndCheck();
  }

  private void timeAndCheck() throws Exception {
    if (_parallel) {
      timeAndCheckParallel();
    } else {
      timeAndCheckSequential();
    }
  }

  private void timeAndCheckParallel() throws Exception {
    DecimalFormat df = new DecimalFormat("#.####");
    Runtime javaApp = Runtime.getRuntime();
    int nProcs = Math.max(javaApp.availableProcessors(), 1);
    ExecutorService execService = Executors.newFixedThreadPool(nProcs);
    int numTestPerThread = _numTests / nProcs;
    Callable<Long> task  = timeAndCheckCallable(numTestPerThread);
    List<Callable<Long>> tasks = Collections.nCopies(nProcs, task);

    List<Future<Long>> futures = execService.invokeAll(tasks);
    Long totalExecTime = futures.stream()
            .map((future) -> {
              try {
                return future.get();
              } catch (Exception e){
                throw new IllegalStateException(e);
              }
            })
            .reduce((a,b) -> a + b).orElse(0L);
    System.out.println(String.format("%s %50s %s", "DEBUG: ", _algDescription, " took "
            + df.format(totalExecTime * 1.0 / _numTests)
            + " nanoseconds on average for " + _numTests * 1.0 / Math.pow(10, 6) + " million tests"));
    execService.shutdown();
  }

  // TODO: Allow running sequentially without spawning new threads. Helps with debugging sometimes.
  private Callable<Long> timeAndCheckCallable(int numTests) {
    return () -> {
      long total = 0, start;
      for (int i = 0; i < numTests; ++i) {
        CloneableInputsMap inputs = _formInput.call();
        CloneableInputsMap orig_inputs = new CloneableInputsMap();
        inputs.forEach((name, inputType) -> {
          try {
            orig_inputs.put(name, inputType.cloneInput());
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
        start = System.nanoTime();
        outputType algorithmResult = _runAlgorithm.apply(inputs);
        total += System.nanoTime() - start;
        CloneableInputsMap algExtraResults = _saveExtraAlgResults.apply(inputs);
        assert (check(orig_inputs, algorithmResult, algExtraResults));
      }
      return total;
    };
  }

  public long timeAndCheckSequential() throws Exception {
    return timeAndCheckCallable(_numTests).call();
  }

  public void setKnownOutput(Function<CloneableInputsMap, outputType> getKnownOutput) {
    _getKnownOutput = getKnownOutput;
    knownOutputSet = true;
  }

  public void setSequential() {
    _parallel = false;
  }
  public void saveExtraAlgResults(Function<CloneableInputsMap, CloneableInputsMap> saveExtraAlgResults) {
    _saveExtraAlgResults = saveExtraAlgResults;
    extraAlgResultsSaved = true;
  }

  public void saveExtraExpResults(Function<CloneableInputsMap, CloneableInputsMap> saveExtraExpResults) {
    _saveExtraExpResults = saveExtraExpResults;
    extraExpResultsSaved = true;
  }

  private boolean check(CloneableInputsMap orig_inputs, outputType algorithmResult, CloneableInputsMap algExtraResults) {
    outputType expectedResult = _getKnownOutput.apply(orig_inputs);
    CloneableInputsMap expectedExtraResults = _saveExtraExpResults.apply(orig_inputs);
    AlgCompleteData<outputType> completeData = new AlgCompleteData<>(orig_inputs, expectedResult, algorithmResult, algExtraResults, expectedExtraResults);
    boolean correct = _algChecker.apply(completeData);
    if(!correct) {
      System.out.println("original inputs: " + completeData._orig_inputs.printInputs());
      System.out.println("expected output: " + completeData._expectedResults);
      System.out.println("observed output: " + completeData._observedResults + "\n");
    }
    return correct;
  }
//
//  public static abstract class InputFormer implements Callable<CloneableInputsMap> {
//    protected CloneableInputsMap inputs;
//    public InputFormer() {
//      inputs = new CloneableInputsMap();
//    }
//  }
//
//  // Example usage
//
//  TimeTests.InputFormer inputFormer = new TimeTests.InputFormer() {
//    @Override
//    public CloneableInputsMap call() throws Exception {
//      Random rgen = new Random();
//      ArrayList<Double> A = MiscHelperMethods.randArray(rgen::nextDouble, ARR_LEN);
//      this.inputs.addArrayList("A", A);
//      return this.inputs;
//    }
//  };
}
