package ru.cellularwildfire.tuner.experiment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class ExperimentResult {
  private final Experiment experiment;
  private final List<ExperimentIteration> iterations;

  public ExperimentResult(Experiment experiment, List<ExperimentIteration> iterations) {
    this.experiment = experiment;
    this.iterations = iterations;
  }

  private static String formatValue(double value) {
    if (value < 0.0001 || 10000000 <= value) {
      return String.format(Locale.US, "%.2e", value);
    }
    if (100 <= Math.abs(value)) {
      int factor = 1;
      long n = Math.abs(Math.round(value));
      while (1000 <= n) {
        n /= 10;
        factor *= 10;
      }
      return String.valueOf(Math.round(value) / factor * factor);
    }
    int scale = 2 - (int) Math.floor(Math.log10(value));
    BigDecimal decimal = new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP);
    return decimal.stripTrailingZeros().toPlainString();
  }

  private static String formatPreciseValue(double value) {
    if (value < 0.00001 || 10000000 <= value) {
      return String.format(Locale.US, "%.3e", value);
    }
    if (1000 <= Math.abs(value)) {
      int factor = 1;
      long n = Math.abs(Math.round(value));
      while (10000 <= n) {
        n /= 10;
        factor *= 10;
      }
      return String.valueOf(Math.round(value) / factor * factor);
    }
    int scale = 3 - (int) Math.floor(Math.log10(value));
    BigDecimal decimal = new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP);
    return decimal.stripTrailingZeros().toPlainString();
  }

  private static String styledText(String text, int... styles) {
    StringBuilder textBuilder = new StringBuilder(text);
    for (int style : styles) {
      textBuilder = new StringBuilder("\u001B[%dm".formatted(style) + textBuilder + "\u001B[0m");
    }
    text = textBuilder.toString();
    return text;
  }

  public void print() {
    List<Criterion> criteria = experiment.getTuneTask().getCriteria();
    List<ModelParameter> parameters = experiment.getTuneTask().getParameters();

    System.out.println(
        "\n" + styledText(experiment.getTuneTask().getName(), TextStyle.BOLD, TextStyle.CYAN));

    System.out.println();
    ExperimentIteration bestIteration =
        iterations.stream().max(ExperimentIteration::compareByScore).orElseThrow();
    int scoreStyle = bestIteration.hasFailures() ? TextStyle.YELLOW : TextStyle.GREEN;
    scoreStyle = bestIteration.hasWeightedFailures() ? TextStyle.RED : scoreStyle;
    System.out.print(styledText("Best total weighted score: ", TextStyle.BOLD, scoreStyle));
    String formattedScore = String.format(Locale.US, "%.2f", bestIteration.countScore());
    System.out.println(styledText(formattedScore, TextStyle.BOLD, scoreStyle) + "\n");

    for (int tuneCaseIndex = 0;
        tuneCaseIndex < bestIteration.getCaseScores().size();
        tuneCaseIndex++) {
      Criterion.ModelScore modelScore = bestIteration.getCaseScores().get(tuneCaseIndex);
      int colorStyle;
      if (modelScore.isFailure()) {
        colorStyle = TextStyle.RED;
        formattedScore = "FAIL";
      } else {
        colorStyle = modelScore.getScore() < 0.4 ? TextStyle.YELLOW : TextStyle.GREEN;
        formattedScore = String.format(Locale.US, "%.2f", modelScore.getScore());
      }
      System.out.print(styledText(formattedScore, TextStyle.BOLD, colorStyle) + "  ");
      String tuneCaseName = criteria.get(tuneCaseIndex).getName();
      System.out.print(styledText(tuneCaseName, TextStyle.CYAN));
      Optional<String> message = bestIteration.getCaseScores().get(tuneCaseIndex).getMessage();
      message.ifPresent(text -> System.out.print("  " + styledText(text, colorStyle)));
      System.out.println();
    }
    System.out.println();

    if (bestIteration.hasWeightedFailures() || experiment.countIterations() == 1) {
      return;
    }
    System.out.println(styledText("Optimal parameters", TextStyle.BOLD, TextStyle.PURPLE));
    for (int parameterIndex = 0; parameterIndex < parameters.size(); parameterIndex++) {
      ModelParameter parameter = parameters.get(parameterIndex);
      if (parameter.isFixed()) {
        continue;
      }
      double value =
          parameter
              .getVariations()
              .get(bestIteration.getParameterValueIndices().get(parameterIndex));
      System.out.print(styledText(parameter.getName(), TextStyle.PURPLE));
      System.out.println(
          "    " + styledText(formatPreciseValue(value), TextStyle.BOLD, TextStyle.BLUE));
    }
    System.out.println();

    System.out.println(styledText("Parameter ranges", TextStyle.BOLD, TextStyle.PURPLE));
    for (int parameterIndex = 0; parameterIndex < parameters.size(); parameterIndex++) {
      ModelParameter parameter = parameters.get(parameterIndex);
      if (parameter.isFixed()) {
        continue;
      }
      int rangeSize = parameter.getVariations().size();
      int[] counts = new int[rangeSize];
      for (ExperimentIteration iteration : iterations) {
        if (!iteration.hasWeightedFailures()) {
          counts[iteration.getParameterValueIndices().get(parameterIndex)]++;
        }
      }
      int maxCount = Arrays.stream(counts).max().orElseThrow();
      int totalCount = Arrays.stream(counts).sum();
      System.out.print(styledText(parameter.getName(), TextStyle.PURPLE) + "  ");
      for (int valueIndex = 0; valueIndex < rangeSize; valueIndex++) {
        int colorStyle = counts[valueIndex] == maxCount ? TextStyle.GREEN : TextStyle.YELLOW;
        if (valueIndex == 0 || valueIndex == rangeSize - 1) {
          colorStyle = counts[valueIndex] > 0 ? colorStyle : TextStyle.RED;
        } else if (counts[valueIndex] == 0) {
          continue;
        }
        double value = parameters.get(parameterIndex).getVariations().get(valueIndex);
        String text = "  " + styledText(formatValue(value), TextStyle.BOLD, colorStyle);
        if (counts[valueIndex] > 0) {
          String formattedPercentage = "(%d%%)".formatted(100 * counts[valueIndex] / totalCount);
          text += styledText(formattedPercentage, TextStyle.BOLD, TextStyle.GRAY);
        }
        System.out.print(text);
        if (valueIndex == rangeSize - 1) {
          System.out.println();
        }
      }
    }
    System.out.println();
  }

  private static final class TextStyle {
    public static final int BOLD = 1;
    public static final int RED = 31;
    public static final int GREEN = 32;
    public static final int YELLOW = 33;
    public static final int BLUE = 34;
    public static final int PURPLE = 35;
    public static final int CYAN = 36;
    public static final int GRAY = 90;
  }
}
