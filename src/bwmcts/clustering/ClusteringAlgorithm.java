package bwmcts.clustering;

import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Unit;

public interface ClusteringAlgorithm {

	HashMap<Integer, List<Unit>> getClusters(Unit[] units, int numClusters, double hpMultiplier);

}
