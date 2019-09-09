package dk.alexandra.fresco.calculator;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  @Test
  public void test_calculator() throws Exception {
    runTest(new CalculatorTests.TestCalculator<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

}
