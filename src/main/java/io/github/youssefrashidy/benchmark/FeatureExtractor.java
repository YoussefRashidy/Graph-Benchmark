package io.github.youssefrashidy.benchmark;

import io.github.youssefrashidy.annotations.Component;
import io.github.youssefrashidy.benchmark.model.*;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Component
public class FeatureExtractor {

    List<RunStats> extractMSTFeature(List<MSTComparison> list) {
        List<RunStats> runStatsList = FastList.newList();
        list.forEach(mstComparison -> {
            var prim = mstComparison.prim();
            var kruskal = mstComparison.kruskal();
            var primSummary = summaryStatistics(prim);
            var kruskalSummary = summaryStatistics(kruskal);
            var kruskalStat = new RunStats(kruskal.algorithm(), kruskal.distribution(), kruskal.v(), kruskalSummary.getAverage(), median(kruskal), std(kruskal, kruskalSummary), 1);
            var primStat = new RunStats(prim.algorithm(), prim.distribution(), prim.v(), primSummary.getAverage(), median(prim), std(prim, primSummary), primSummary.getAverage() / kruskalSummary.getAverage());
            runStatsList.add(primStat);
            runStatsList.add(kruskalStat);
        });
        return runStatsList;
    }

    List<RunStats> extractSSSPGeneralFeature(List<SSSPGeneral> list) {
        List<RunStats> runStatsList = FastList.newList();
        list.forEach(ssspGeneral -> {
            var dijkstra = ssspGeneral.dijkstra();
            var dijkstraSummary = summaryStatistics(dijkstra);
            var dijkstraStat = new RunStats(
                    dijkstra.algorithm(),
                    dijkstra.distribution(),
                    dijkstra.v(),
                    dijkstraSummary.getAverage(),
                    median(dijkstra),
                    std(dijkstra, dijkstraSummary),
                    1 // no comparison, single algorithm
            );
            runStatsList.add(dijkstraStat);
        });
        return runStatsList;
    }

    List<RunStats> extractSSSPDAGFeature(List<SSSPComparison> list) {
        List<RunStats> runStatsList = FastList.newList();
        list.forEach(ssspComparison -> {
            var dijkstra = ssspComparison.dijkstra();
            var dag = ssspComparison.dag();
            var dijkstraSummary = summaryStatistics(dijkstra);
            var dagSummary = summaryStatistics(dag);
            var dijkstraStat = new RunStats(
                    dijkstra.algorithm(),
                    dijkstra.distribution(),
                    dijkstra.v(),
                    dijkstraSummary.getAverage(),
                    median(dijkstra),
                    std(dijkstra, dijkstraSummary),
                    1
            );
            var dagStat = new RunStats(
                    dag.algorithm(),
                    dag.distribution(),
                    dag.v(),
                    dagSummary.getAverage(),
                    median(dag),
                    std(dag, dagSummary),
                    dagSummary.getAverage() / dijkstraSummary.getAverage()
            );
            runStatsList.add(dijkstraStat);
            runStatsList.add(dagStat);
        });
        return runStatsList;
    }

    DoubleSummaryStatistics summaryStatistics(SingleRun singleRun) {
        return Arrays.stream(singleRun.times()).asDoubleStream().summaryStatistics();
    }

    double std(SingleRun singleRun, DoubleSummaryStatistics summaryStatistics) {
        return Math.sqrt(
                Arrays.stream(singleRun.times())
                        .mapToDouble(time -> time - summaryStatistics.getAverage())
                        .map(time -> time * time)
                        .sum() / (summaryStatistics.getCount() - 1)
        );
    }

    double median(SingleRun singleRun) {
        Arrays.sort(singleRun.times());
        int length = singleRun.times().length;
        if (length % 2 == 0) {
            long median1 = singleRun.times()[length / 2 - 1];
            long median2 = singleRun.times()[length / 2];
            return (double) (median1 + median2) / 2;
        } else {
            return singleRun.times()[length / 2];
        }
    }
}
