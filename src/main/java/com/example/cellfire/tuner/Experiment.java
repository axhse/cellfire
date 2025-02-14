package com.example.cellfire.tuner;

import com.example.cellfire.tuner.cases.TuneCase;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Experiment {
    private static final int LIMIT_ITERATIONS = 1_000_000;
    private final String name;
    private final boolean isFastToFail;
    private final List<TuneCase> tuneCases;
    private final List<ModelParameter> parameters;
    private final int iterationQuantity;
    private final List<Iteration> iterations = new ArrayList<>();

    public Experiment(String name, boolean isFastToFail, List<TuneCase> tuneCases, List<ModelParameter> parameters) {
        this.name = name;
        this.isFastToFail = isFastToFail;
        this.tuneCases = tuneCases;
        this.parameters = parameters;
        iterationQuantity = this.countIterations();
    }

    public void run() {
        for (int iterationIndex = 0; iterationIndex < iterationQuantity; iterationIndex++) {
            int n = iterationIndex;
            List<Integer> parameterValueIndices = new ArrayList<>();
            List<Double> caseScores = new ArrayList<>();
            for (ModelParameter parameter : parameters) {
                int parameterValueIndex = n % parameter.getRange().size();
                double parameterValue = parameter.getRange().get(parameterValueIndex);
                parameter.setValue(parameterValue);
                parameterValueIndices.add(parameterValueIndex);
                n /= parameter.getRange().size();
            }
            for (TuneCase tuneCase : tuneCases) {
                double score = tuneCase.evaluate();
                caseScores.add(score);
                if (score < 0 && isFastToFail) {
                    break;
                }
            }
            this.iterations.add(new Iteration(parameterValueIndices, caseScores));
        }
    }

    public void printResults() {
        System.out.println();
        System.out.println();
        System.out.println(styledText(name, TextStyle.BOLD, TextStyle.CYAN));

        System.out.println();
        Iteration bestIteration = iterations.stream().max(Experiment::compareIterations).get();
        if (bestIteration.isFailed()) {
            System.out.print(styledText("Failures: ", TextStyle.RED));
            System.out.println(styledText(Integer.toString(bestIteration.getFailureCount()), TextStyle.BOLD, TextStyle.RED));
            for (int tuneCaseIndex = 0; tuneCaseIndex < bestIteration.getCaseScores().size(); tuneCaseIndex++) {
                if (bestIteration.getCaseScores().get(tuneCaseIndex) < 0) {
                    System.out.print(styledText(tuneCases.get(tuneCaseIndex).getName(), TextStyle.RED));
                }
            }
        } else {
            System.out.print(styledText("Total score: ", TextStyle.BOLD, TextStyle.GREEN));
            System.out.println(styledText(Double.toString(bestIteration.getTotalScore()), TextStyle.BOLD, TextStyle.GREEN, TextStyle.GREEN));
            for (int tuneCaseIndex = 0; tuneCaseIndex < bestIteration.getCaseScores().size(); tuneCaseIndex++) {
                double score = bestIteration.getCaseScores().get(tuneCaseIndex);
                int colorStyle = score > 0.2 ? TextStyle.GREEN : TextStyle.YELLOW;
                System.out.print(styledText(Double.toString(Math.round(score)) + "    ", TextStyle.BOLD, colorStyle));
                System.out.println(styledText(tuneCases.get(tuneCaseIndex).getName(), TextStyle.CYAN));
            }
        }

        if (bestIteration.isFailed()) {
            return;
        }

        System.out.println();
        System.out.println(styledText("Parameters", TextStyle.BOLD, TextStyle.PURPLE));
        for (int parameterIndex = 0; parameterIndex < parameters.size(); parameterIndex++) {
            ModelParameter parameter = parameters.get(parameterIndex);
            double value = parameter.getRange().get(bestIteration.getParameterValueIndices().get(parameterIndex));
            System.out.print(styledText(parameter.getFieldName(), TextStyle.PURPLE));
            System.out.println("    " + styledText(formatValue(value), TextStyle.BOLD, TextStyle.BLUE));
        }

        System.out.println();
        System.out.println(styledText("Parameter ranges", TextStyle.BOLD, TextStyle.PURPLE));
        for (int parameterIndex = 0; parameterIndex < parameters.size(); parameterIndex++) {
            ModelParameter parameter = parameters.get(parameterIndex);
            int rangeSize = parameter.getRange().size();
            int[] counts = new int[rangeSize];
            for (Iteration iteration : iterations) {
                if (!iteration.isFailed()) {
                    counts[iteration.getParameterValueIndices().get(parameterIndex)]++;
                }
            }
            var maxCount = Arrays.stream(counts).max().getAsInt();
            int totalCount = Arrays.stream(counts).sum();
            String valueLine = "  ";
            for (int valueIndex = 0; valueIndex < rangeSize; valueIndex++) {
                int colorStyle = counts[valueIndex] == maxCount ? TextStyle.GREEN : TextStyle.YELLOW;
                if (valueIndex == 0 || valueIndex == rangeSize - 1) {
                    colorStyle = counts[valueIndex] > 0 ? colorStyle : TextStyle.RED;
                } else if (counts[valueIndex] == 0) {
                    continue;
                }
                double value = parameters.get(parameterIndex).getRange().get(valueIndex);
                String text = "  " + styledText(formatValue(value), TextStyle.BOLD, colorStyle);
                if (counts[valueIndex] > 0) {
                    text += styledText("(%d%%)".formatted(100 * counts[valueIndex] / totalCount), TextStyle.BOLD, TextStyle.GRAY);
                }
                valueLine += text;
            }
            System.out.print(styledText(parameter.getFieldName(), TextStyle.PURPLE));
            System.out.println(valueLine);
        }
    }

    private int countIterations() {
        int iterationQuantity = 1;
        for (ModelParameter parameter : parameters) {
            iterationQuantity *= parameter.getRange().size();
            if (iterationQuantity > LIMIT_ITERATIONS) {
                throw new IllegalArgumentException("Too many iterations");
            }
        }
        return iterationQuantity;
    }

    private static String formatValue(double value) {
        if (10 <= Math.abs(value)) {
            int factor = 1;
            long n = Math.abs(Math.round(value));
            while (100 <= n) {
                n /= 10;
                factor *= 10;
            }
            return String.valueOf(Math.round(value) / factor * factor);
        }
        BigDecimal decimal = new BigDecimal(value).setScale(1, RoundingMode.HALF_UP);
        return decimal.stripTrailingZeros().toPlainString();
    }

    private static String styledText(String text, int... styles) {
        for (int style : styles) {
            text = "\u001B[%dm".formatted(style) + text + "\u001B[0m";
        }
        return text;
    }

    private static int compareIterations(Iteration iteration1, Iteration iteration2) {
        if (iteration1.getFailureCount() == iteration2.getFailureCount()) {
            if (iteration1.getTotalScore() == iteration2.getTotalScore()) {
                return 0;
            }
            return iteration1.getTotalScore() < iteration2.getTotalScore() ? -1 : 1;
        }
        return iteration1.getFailureCount() > iteration2.getFailureCount() ? -1 : 1;
    }


    public static final class TextStyle {
        public static final int NONE = 0;
        public static final int BOLD = 1;
        public static final int RED = 31;
        public static final int GREEN = 32;
        public static final int YELLOW = 33;
        public static final int BLUE = 34;
        public static final int PURPLE = 35;
        public static final int CYAN = 36;
        public static final int GRAY = 90;
    }

    public static final class ModelParameter {
        private final Class<?> fieldClass;
        private final String fieldName;
        private final List<Double> range;

        public ModelParameter(Class<?> fieldClass, String fieldName, List<Double> range) {
            this.fieldClass = fieldClass;
            this.fieldName = fieldName;
            this.range = range;
        }

        public List<Double> getRange() {
            return range;
        }

        public void setValue(Double value) {
            try {
                Field field = fieldClass.getDeclaredField(this.fieldName);
                field.setAccessible(true);
                field.setDouble(null, value);
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                throw new IllegalArgumentException(exception);
            }
        }

        public String getFieldName() {
            return fieldName;
        }

        public static List<Double> singleValue(double value) {
            return List.of(value);
        }

        public static List<Double> range(double min, double max, int steps) {
            if (max <= min || steps == 1) {
                return List.of(min);
            }
            List<Double> values = new ArrayList<>();
            for (int i = 0; i <= steps; i++) {
                values.add(min + (max - min) / steps * i);
            }
            return values;
        }
    }

    private static final class Iteration {
        private final List<Integer> parameterValueIndices;
        private final List<Double> caseScores;
        private final int failureCount;
        private final double totalScore;

        public Iteration(List<Integer> parameterValueIndices, List<Double> caseScores) {
            this.parameterValueIndices = parameterValueIndices;
            this.caseScores = caseScores;
            this.failureCount = (int)caseScores.stream().filter(score -> score < 0).count();
            this.totalScore = caseScores.stream().reduce(0.0, Double::sum);
        }

        public List<Integer> getParameterValueIndices() {
            return parameterValueIndices;
        }

        public List<Double> getCaseScores() {
            return caseScores;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public boolean isFailed() {
            return failureCount > 0;
        }

        public double getTotalScore() {
            return totalScore;
        }
    }
}
