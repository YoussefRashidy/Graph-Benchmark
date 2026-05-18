package io.github.youssefrashidy.benchmark;

import io.github.youssefrashidy.annotations.Component;
import io.github.youssefrashidy.benchmark.model.RunStats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class CSVExporter {
    private static final String HEADER = "algorithm,distribution,v,mean,median,stdDev,speedup";

    public void exportCsv(List<RunStats> stats, String path) {
        StringBuilder builder = new StringBuilder(HEADER);
        builder.append("\n");
        stats.forEach(stat -> {
            builder.append(stat.algorithm()).append(",")
                    .append(stat.distribution()).append(",")
                    .append(stat.v()).append(",")
                    .append(stat.meanNanos()).append(",")
                    .append(stat.medianNanos()).append(",")
                    .append(stat.stdDevNanos()).append(",")
                    .append(stat.speedUp()).append("\n");
        });

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(builder.toString());
        } catch (IOException ioException) {
            throw new RuntimeException("Failed to write CSV to " + path, ioException);
        }
    }
}
