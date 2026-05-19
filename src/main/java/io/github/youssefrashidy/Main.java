package io.github.youssefrashidy;

import io.github.youssefrashidy.benchmark.BenchmarkOrchestrator;
import io.github.youssefrashidy.benchmark.CSVExporter;
import io.github.youssefrashidy.benchmark.FeatureExtractor;
import io.github.youssefrashidy.context.AnnotationConfigApplicationContext;
import io.github.youssefrashidy.context.ApplicationContext;
import io.github.youssefrashidy.gshell.GShell;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        ApplicationContext context = new AnnotationConfigApplicationContext(
                Set.of(
                        "io.github.youssefrashidy.benchmark",
                        "io.github.youssefrashidy.graph",
                        "io.github.youssefrashidy.gshell"
                )
        );
        while (true) {
            System.out.println("Choose one of the following options");
            System.out.println("1.Graph Shell");
            System.out.println("2.MST benchmark");
            System.out.println("3.Dijkstra benchmark");
            System.out.println("4.SSSP-DAG benchmark");
            System.out.println("5.All benchmark");
            int option = -1;
            try {
                option = scanner.nextInt();
                scanner.nextLine(); // To consume remaining \n
            } catch (RuntimeException e) {
                System.out.println("Invalid option");
            }
            switch (option) {
                case 1 -> {
                    GShell shell = new GShell();
                    shell.shell();
                }
                case 2 -> {
                    System.out.println("Input file output name/path (Default results.csv) : ");
                    String path = scanner.nextLine();
                    if (path.isEmpty()) path = "results.csv";
                    if (!path.endsWith(".csv")) path += ".csv";
                    BenchmarkOrchestrator benchmarker = context.getInstance(BenchmarkOrchestrator.class);
                    var results = benchmarker.compareMST();
                    FeatureExtractor featureExtractor = new FeatureExtractor();
                    var features = featureExtractor.extractMSTFeature(results);
                    CSVExporter csvExporter = new CSVExporter();
                    try {
                        csvExporter.exportCsv(features, path);
                        System.out.println("CSV written to : " + Path.of(path).toAbsolutePath());
                        // open Excel sheet in a new process
                    } catch (RuntimeException e){
                        System.out.println("Failed to write results to CSV file.");
                    }

                }
                case 3 -> {
                    System.out.println("Input file output name/path (Default results.csv) : ");
                    String path = scanner.nextLine();
                    if (path.isEmpty()) path = "results.csv";
                    if (!path.endsWith(".csv")) path += ".csv";
                    BenchmarkOrchestrator benchmarker = context.getInstance(BenchmarkOrchestrator.class);
                    var results = benchmarker.runSSSPGeneral();
                    FeatureExtractor featureExtractor = new FeatureExtractor();
                    var features = featureExtractor.extractSSSPGeneralFeature(results);
                    CSVExporter csvExporter = new CSVExporter();
                    try {
                        csvExporter.exportCsv(features, path);
                        System.out.println("CSV written to : " + Path.of(path).toAbsolutePath());
                    } catch (RuntimeException e){
                        System.out.println("Failed to write results to CSV file.");
                    }

                }
                case 4 -> {
                    System.out.println("Input file output name/path (Default results.csv) : ");
                    String path = scanner.nextLine();
                    if (path.isEmpty()) path = "results.csv";
                    if (!path.endsWith(".csv")) path += ".csv";
                    BenchmarkOrchestrator benchmarker = context.getInstance(BenchmarkOrchestrator.class);
                    var results = benchmarker.compareSSSP();
                    FeatureExtractor featureExtractor = new FeatureExtractor();
                    var features = featureExtractor.extractSSSPDAGFeature(results);
                    CSVExporter csvExporter = new CSVExporter();
                    try {
                        csvExporter.exportCsv(features, path);
                        System.out.println("CSV written to : " + Path.of(path).toAbsolutePath());
                    } catch (RuntimeException e){
                        System.out.println("Failed to write results to CSV file.");
                    }

                }
                case 5 -> {
                    System.out.println("Input file output base name/path (Default results) : ");
                    String basePath = scanner.nextLine();
                    if (basePath.isEmpty()) basePath = "results";
                    BenchmarkOrchestrator benchmarker = context.getInstance(BenchmarkOrchestrator.class);
                    var result = benchmarker.runAll();
                    FeatureExtractor featureExtractor = new FeatureExtractor();
                    CSVExporter csvExporter = new CSVExporter();

                    String mstPath = basePath + "-mst.csv";
                    String ssspPath = basePath + "-sssp.csv";
                    String dagPath = basePath + "-dag.csv";

                    try {
                        csvExporter.exportCsv(featureExtractor.extractMSTFeature(result.mstComparisons()), mstPath);
                        csvExporter.exportCsv(featureExtractor.extractSSSPGeneralFeature(result.ssspGeneral()), ssspPath);
                        csvExporter.exportCsv(featureExtractor.extractSSSPDAGFeature(result.ssspComparisons()), dagPath);
                        System.out.println("CSV written to : " + Path.of(mstPath).toAbsolutePath());
                        System.out.println("CSV written to : " + Path.of(ssspPath).toAbsolutePath());
                        System.out.println("CSV written to : " + Path.of(dagPath).toAbsolutePath());
                    } catch (RuntimeException e){
                        System.out.println("Failed to write results to CSV file.");
                    }

                }
                default -> System.out.println("Invalid option");

            }


        }
    }
}