# 09: Cluster-aware population

**What to build:** A Hazelcast-backed Blotter behaves correctly when the application runs on more than one node sharing one Hazelcast cluster. Today population clears the shared map, while the `populated` state and the population lock exist only inside one node's JVM. So a node that starts while the cluster is serving rebuilds the map under the other nodes' feet, and a lazy node ignores refreshes while its own flag says unpopulated, even though the shared map already holds data. See ADR-0008 (Known limitation: multi-node clusters) and spec.md.

Make population, refresh and rebuild coordinate across the cluster, so that:

- A node joining a live cluster whose map is already populated does not clear or rebuild it (eager mode included).
- Only one node populates at a time, cluster-wide. Nodes that start together, or call the rebuild service together, do not run concurrent clears and fills of the same map.
- Whether a map is populated is a cluster-wide fact, not a per-node flag. A lazy node whose map another node has already filled serves it, and applies refreshes to it.
- A refresh or delete that arrives anywhere in the cluster while a population is running is not lost and cannot resurrect a deleted row (the guarantee ticket 04 and the review fixes gave within one node, now across nodes).
- Viewers never see a partial set because another node is rebuilding. They get the previous complete set or the new complete set. The generated rebuild service is the one deliberate way to replace a populated map.
- If the node doing the population dies mid-way, the map is not left marked populated, and another node can take over.
- `maxRows` is still checked before anything is stored, and a failure leaves the map unpopulated for every node.

Keep the single-node behaviour and its tests unchanged. The mechanism is the implementer's choice. Constraint: confirm what the application's existing Hazelcast instance and its configuration actually support before picking one (for example, a CP-subsystem lock needs a CP group of enough members, which the showcase may not configure; a map-key lock or a marker entry in a Hazelcast structure may suit better). Do not add new infrastructure beyond the existing Hazelcast instance (ADR-0008). If the chosen mechanism needs configuration, say so in the ADR.

**Blocked by:** None (can start immediately; tickets 01 to 08 are merged)

**Status:** ready-for-agent

- [ ] A test starts a second Hazelcast member in the same JVM (or an equivalent two-node setup) against the same cluster. A node that joins a populated map leaves it intact, and fetch-all on both nodes returns the full set throughout.
- [ ] Two nodes started or rebuilt at the same time populate once, with no concurrent clear and fill, and both end up serving the complete set.
- [ ] A lazy node whose map was filled by another node serves it without repopulating, and applies a refresh made on either node.
- [ ] A refresh or delete committed on one node while the other is populating is reflected afterwards, with no ghost row (extend the existing refresh-during-population test to two nodes).
- [ ] If the populating node fails or leaves the cluster mid-population, the map is not reported populated, and another node's next fetch or rebuild populates it.
- [ ] A `maxRows` failure leaves the map unpopulated on every node.
- [ ] The existing single-node Hazelcast integration tests pass unchanged.
- [ ] ADR-0008's "Known limitation: multi-node clusters" paragraph is replaced by a short description of the cluster-aware behaviour and the mechanism chosen, including any configuration it needs.
