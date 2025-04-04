package ru.cellularwildfire.tuner.experiment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ExperimentResult {
  private final Experiment experiment;
  private final List<ExperimentIteration> iterations;

  public ExperimentResult(Experiment experiment, List<ExperimentIteration> iterations) {
    this.experiment = experiment;
    this.iterations = iterations;
  }

  private static String formatValue(double value) {
    if (value < 0.0001 || 10000000 <= value) {
      return String.format(Locale.US, "%.1e", value);
    }
    if (10 <= Math.abs(value)) {
      int factor = 1;
      long n = Math.abs(Math.round(value));
      while (100 <= n) {
        n /= 10;
        factor *= 10;
      }
      return String.valueOf(Math.round(value) / factor * factor);
    }
    int scale = 1 - (int) Math.floor(Math.log10(value));
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

    System.out.println();
    System.out.println(
        styledText(experiment.getTuneTask().getName(), TextStyle.BOLD, TextStyle.CYAN));

    System.out.println();
    ExperimentIteration bestIteration =
        iterations.stream().max(ExperimentIteration::compareByScore).orElseThrow();
    if (bestIteration.hasFailures()) {
      System.out.print(styledText("Failures: ", TextStyle.BOLD, TextStyle.RED));
      String formattedFailureCount = Integer.toString(bestIteration.countFailures());
      System.out.println(styledText(formattedFailureCount, TextStyle.BOLD, TextStyle.RED));
      for (int tuneCaseIndex = 0;
          tuneCaseIndex < bestIteration.getCaseScores().size();
          tuneCaseIndex++) {
        if (bestIteration.getCaseScores().get(tuneCaseIndex).isFailure()) {
          String title = criteria.get(tuneCaseIndex).getName() + "  ";
          System.out.print(styledText(title, TextStyle.CYAN));
          String description =
              bestIteration.getCaseScores().get(tuneCaseIndex).getFailureDescription();
          System.out.println(styledText(description, TextStyle.RED));
        }
      }
      System.out.println();
    }
    int scoreStyle = bestIteration.hasFailures() ? TextStyle.YELLOW : TextStyle.GREEN;
    System.out.print(styledText("Total score: ", TextStyle.BOLD, scoreStyle));
    String formattedScore = String.format(Locale.US, "%.2f", bestIteration.countScore());
    System.out.println(styledText(formattedScore, TextStyle.BOLD, scoreStyle));
    for (int tuneCaseIndex = 0;
        tuneCaseIndex < bestIteration.getCaseScores().size();
        tuneCaseIndex++) {
      Criterion.ModelScore modelScore = bestIteration.getCaseScores().get(tuneCaseIndex);
      if (modelScore.isFailure()) {
        continue;
      }
      int colorStyle = modelScore.getScore() > 0.2 ? TextStyle.GREEN : TextStyle.YELLOW;
      formattedScore = String.format(Locale.US, "%.2f", modelScore.getScore());
      System.out.print(styledText(formattedScore, TextStyle.BOLD, colorStyle));
      String tuneCaseName = criteria.get(tuneCaseIndex).getName();
      System.out.println(styledText("  " + tuneCaseName, TextStyle.CYAN));
    }
    System.out.println();

    if (bestIteration.hasFailures() || experiment.countIterations() == 1) {
      return;
    }
    System.out.println(styledText("Parameters", TextStyle.BOLD, TextStyle.PURPLE));
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
      System.out.println("    " + styledText(formatValue(value), TextStyle.BOLD, TextStyle.BLUE));
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
        if (!iteration.hasFailures()) {
          counts[iteration.getParameterValueIndices().get(parameterIndex)]++;
        }
      }
      int maxCount = Arrays.stream(counts).max().orElseThrow();
      int totalCount = Arrays.stream(counts).sum();
      System.out.print(styledText(parameter.getName() + "  ", TextStyle.PURPLE));
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
