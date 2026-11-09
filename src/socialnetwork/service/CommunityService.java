
package socialnetwork.service;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.repository.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Computes connected communities and their longest simple paths.
 */
public class CommunityService {

    private final Repository<Long, User> users;

    private final Repository<Tuple<Long, Long>, Friendship> friendships;

    /**
     * Creates the community service.
     *
     * @param users the user repository
     * @param friendships the friendship repository
     */
    public CommunityService(
            Repository<Long, User> users,
            Repository<Tuple<Long, Long>, Friendship> friendships) {

        this.users = Objects.requireNonNull(users);
        this.friendships = Objects.requireNonNull(friendships);
    }

    /**
     * Builds an undirected adjacency map.
     * Isolated users are included.
     *
     * @return the adjacency map
     */
    private Map<Long, Set<Long>> buildGraph() {

        Set<Long> userIds = users.findAll()
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        List<Friendship> validFriendships =
                friendships.findAll()
                        .stream()
                        .filter(friendship ->
                                userIds.contains(friendship.getUserId1())
                                        && userIds.contains(friendship.getUserId2())
                        )
                        .collect(Collectors.toList());

        return userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> validFriendships.stream()
                                .filter(friendship ->
                                        friendship.getUserId1().equals(id)
                                                || friendship.getUserId2().equals(id)
                                )
                                .flatMap(friendship ->
                                        Stream.of(
                                                friendship.getUserId1(),
                                                friendship.getUserId2()
                                        )
                                )
                                .filter(neighbor -> !neighbor.equals(id))
                                .collect(Collectors.toCollection(TreeSet::new)),
                        (first, second) -> first,
                        TreeMap::new
                ));
    }

    /**
     * Performs recursive depth-first traversal.
     *
     * @param node the current node
     * @param graph the adjacency map
     * @param visited visited nodes in this traversal
     */
    private void dfs(
            Long node,
            Map<Long, Set<Long>> graph,
            Set<Long> visited) {

        if (!visited.add(node)) {
            return;
        }

        graph.get(node)
                .stream()
                .filter(neighbor -> !visited.contains(neighbor))
                .forEach(neighbor ->
                        dfs(neighbor, graph, visited)
                );
    }

    /**
     * Finds all connected components.
     *
     * @return communities ordered by their smallest user ID
     */
    public List<Set<Long>> getCommunities() {

        Map<Long, Set<Long>> graph = buildGraph();

        Map<Long, Set<Long>> grouped =
                graph.keySet()
                        .stream()
                        .collect(Collectors.groupingBy(
                                node -> {
                                    Set<Long> component = new TreeSet<>();

                                    dfs(node, graph, component);

                                    return component.iterator().next();
                                },
                                TreeMap::new,
                                Collectors.toCollection(TreeSet::new)
                        ));

        return new ArrayList<>(grouped.values());
    }

    /**
     * Returns the number of connected communities.
     *
     * @return the component count
     */
    public int getNumberOfCommunities() {
        return getCommunities().size();
    }

    /**
     * Returns the community containing a user.
     *
     * @param userId the user identifier
     * @return the user's community
     * @throws IllegalArgumentException if the user is missing
     */
    public Set<Long> getCommunityOfUser(Long userId) {

        if (!users.findOne(userId).isPresent()) {
            throw new IllegalArgumentException(
                    "Utilizatorul nu exista."
            );
        }

        return getCommunities()
                .stream()
                .filter(component -> component.contains(userId))
                .findFirst()
                .orElse(Collections.emptySet());
    }

    /**
     * Finds the longest simple path starting from a node.
     *
     * @param node the starting node
     * @param graph the adjacency map
     * @param visited nodes in the current path
     * @return the maximum number of edges
     */
    private int longestPathFrom(
            Long node,
            Map<Long, Set<Long>> graph,
            Set<Long> visited) {

        visited.add(node);

        int maximum = graph.get(node)
                .stream()
                .filter(neighbor -> !visited.contains(neighbor))
                .mapToInt(neighbor ->
                        1 + longestPathFrom(
                                neighbor,
                                graph,
                                visited
                        )
                )
                .max()
                .orElse(0);

        visited.remove(node);

        return maximum;
    }

    /**
     * Finds the longest simple path in a component.
     *
     * @param component the connected component
     * @param graph the adjacency map
     * @return the maximum number of edges
     */
    private int longestPathLength(
            Set<Long> component,
            Map<Long, Set<Long>> graph) {

        return component.stream()
                .mapToInt(node ->
                        longestPathFrom(
                                node,
                                graph,
                                new HashSet<>()
                        )
                )
                .max()
                .orElse(0);
    }

    /**
     * Finds the most sociable community.
     * Communities are compared by longest simple path.
     *
     * @return the most sociable community,
     *         or an empty set if there are no users
     */
    public Set<Long> getMostSociableCommunity() {

        Map<Long, Set<Long>> graph = buildGraph();

        return getCommunities()
                .stream()
                .max(
                        Comparator.comparingInt(
                                (Set<Long> component) ->
                                        longestPathLength(component, graph)
                        ).thenComparing(
                                component -> component.iterator().next(),
                                Comparator.reverseOrder()
                        )
                )
                .map(TreeSet::new)
                .map(component -> (Set<Long>) component)
                .orElseGet(Collections::emptySet);
    }

    /**
     * Returns the longest path length in the most
     * sociable community.
     *
     * @return the number of edges in the longest path
     */
    public int getMostSociableCommunityPathLength() {

        Set<Long> best = getMostSociableCommunity();

        return longestPathLength(best, buildGraph());
    }
}
