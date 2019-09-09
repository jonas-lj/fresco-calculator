package dk.alexandra.fresco.calculator;

import java.math.BigInteger;

import org.junit.Assert;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;

public class CalculatorTests {

  public static class TestCalculator<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {

          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> x = builder.numeric().known(-2);
            DRes<SInt> y = builder.numeric().known(3);

            Expression exp = new Expression("2*(2y-3x) / 3", "x", x, "y", y);

            DRes<SInt> z = exp.buildComputation(builder);
            return builder.numeric().open(z);
          };
          BigInteger output = runApplication(app);

          int expected = 2 * (2 * 3 - 3 * (-2)) / 3;
          Assert.assertEquals(BigInteger.valueOf(expected), output);
        }
      };
    }
  }

}
