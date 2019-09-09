package dk.alexandra.fresco.calculator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.jonaslindstrom.arithmeticparser.EvaluationException;
import dk.jonaslindstrom.arithmeticparser.Evaluator;
import dk.jonaslindstrom.arithmeticparser.Parser;
import dk.jonaslindstrom.arithmeticparser.Token;
import dk.jonaslindstrom.arithmeticparser.MultiOperator;

/**
 * This computation parses an arithmetic expression and evaluates it sequentially.
 */
public class Expression implements Computation<SInt, ProtocolBuilderNumeric> {

  private Map<String, DRes<SInt>> variables;
  private String expression;

  public Expression(String expression, Map<String, DRes<SInt>> variables) {
    this.variables = variables;
    this.expression = expression;
  }

  public Expression(String expression) {
    this(expression, Collections.emptyMap());
  }

  public Expression(String expression, String x, DRes<SInt> xValue) {
    this(expression, Map.of(x, xValue));
  }

  public Expression(String expression, String x, DRes<SInt> xValue, String y, DRes<SInt> yValue) {
    this(expression, Map.of(x, xValue, y, yValue));
  }

  public Expression(String expression, String x, DRes<SInt> xValue, String y, DRes<SInt> yValue,
      String z, DRes<SInt> zValue) {
    this(expression, Map.of(x, xValue, y, yValue, z, zValue));
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      NumbericBuilderWrapper operators = new NumbericBuilderWrapper(builder);
      Map<String, BinaryOperator<NumberType>> ops =
          Map.of("+", (x, y) -> operators.add(x, y), "-", (x, y) -> operators.subtract(x, y), "*",
              (x, y) -> operators.multiply(x, y), "/", (x, y) -> operators.divide(x, y));

      Map<String, MultiOperator<NumberType>> funcs = Map.of(); // TODO

      Parser<NumberType> parser =
          new Parser<NumberType>(new ArrayList<>(ops.keySet()), new NumberType.Parser());
      Evaluator<NumberType> evaluator =
          new Evaluator<NumberType>(Map.of(), ops, new NumberType.Parser());

      List<Token> tokens = null;
      try {
        tokens = parser.parse(expression, new ArrayList<>(this.variables.keySet()),
            new ArrayList<>(funcs.keySet()));

        Map<String, NumberType> vars = new HashMap<>();
        for (String var : variables.keySet()) {
          vars.put(var, new NumberType(variables.get(var)));
        }
        NumberType result = evaluator.evaluate(tokens, vars);

        if (result.isClosed()) {
          return result.getClosed();
        } else {
          return builder.numeric().known(result.getOpen());
        }

      } catch (ParseException e) {
        throw new RuntimeException(
            "Failed to parse expression '" + expression + "': " + e.getMessage());
      } catch (EvaluationException e) {
        throw new RuntimeException(
            "Failed to evaluate expression '" + tokens + "': " + e.getMessage());
      }
    });
  }

  private class NumbericBuilderWrapper {

    private ProtocolBuilderNumeric builder;

    private NumbericBuilderWrapper(ProtocolBuilderNumeric builder) {
      this.builder = builder;
    }

    private NumberType multiply(NumberType a, NumberType b) {
      if (a.isClosed() && b.isClosed()) {
        return new NumberType(builder.numeric().mult(a.getClosed(), b.getClosed()));
      } else if (a.isClosed() && b.isOpen()) {
        return new NumberType(builder.numeric().mult(b.getOpen(), a.getClosed()));
      } else if (a.isOpen() && b.isClosed()) {
        return new NumberType(builder.numeric().mult(a.getOpen(), b.getClosed()));
      } else {
        return new NumberType(a.getOpen().multiply(b.getOpen()));
      }
    }

    private NumberType subtract(NumberType a, NumberType b) {
      if (a.isClosed() && b.isClosed()) {
        return new NumberType(builder.numeric().sub(a.getClosed(), b.getClosed()));
      } else if (a.isClosed() && b.isOpen()) {
        return new NumberType(builder.numeric().sub(a.getClosed(), b.getOpen()));
      } else if (a.isOpen() && b.isClosed()) {
        return new NumberType(builder.numeric().sub(a.getOpen(), b.getClosed()));
      } else {
        return new NumberType(a.getOpen().subtract(b.getOpen()));
      }
    }

    private NumberType add(NumberType a, NumberType b) {
      if (a.isClosed() && b.isClosed()) {
        return new NumberType(builder.numeric().add(a.getClosed(), b.getClosed()));
      } else if (a.isClosed() && b.isOpen()) {
        return new NumberType(builder.numeric().add(b.getOpen(), a.getClosed()));
      } else if (a.isOpen() && b.isClosed()) {
        return new NumberType(builder.numeric().add(a.getOpen(), b.getClosed()));
      } else {
        return new NumberType(a.getOpen().add(b.getOpen()));
      }
    }

    private NumberType divide(NumberType a, NumberType b) {
      if (a.isClosed() && b.isClosed()) {
        return new NumberType(builder.advancedNumeric().div(a.getClosed(), b.getClosed()));
      } else if (a.isClosed() && b.isOpen()) {
        return new NumberType(builder.advancedNumeric().div(a.getClosed(), b.getOpen()));
      } else if (a.isOpen() && b.isClosed()) {
        return new NumberType(
            builder.advancedNumeric().div(builder.numeric().known(a.getOpen()), b.getClosed()));
      } else {
        return new NumberType(a.getOpen().divide(b.getOpen()));
      }
    }
  }

}
