package dk.alexandra.fresco.calculator;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.jonaslindstrom.arithmeticparser.NumberParser;

/**
 * Instances of this class may contains either a DRes<SInt> or a BigInteger value but not both.
 */
public final class NumberType {

  private final DRes<SInt> closed;
  private final BigInteger open;

  public NumberType(DRes<SInt> number) {
    this.closed = number;
    this.open = null;
  }

  public NumberType(int number) {
    this(BigInteger.valueOf(number));
  }

  public NumberType(BigInteger number) {
    this.closed = null;
    this.open = number;
  }

  public boolean isClosed() {
    return closed != null;
  }

  public boolean isOpen() {
    return !isClosed();
  }

  public DRes<SInt> getClosed() {
    return closed;
  }

  public BigInteger getOpen() {
    return open;
  }

  public static class Parser implements NumberParser<NumberType> {

    @Override
    public NumberType parse(String s) throws NumberFormatException {
      return new NumberType(new BigInteger(s));
    }

  }
}
