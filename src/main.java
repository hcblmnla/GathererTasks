import gatherer.GathererTasks;
import gatherer.GathererTasksImpl;

/*
Output:
	[(1 times 2), (2 times 3), (3 times 4), (3 times 1), (1 times 10)]
	[2, 7, 3, 10, 4, 5, 1, 10]
	[[2], [7, 3], [10, 4, 4], [5, 5, 1], [10]]
	[[2], [10, 4, 4], [10]]
	[[2, 7], [3, 10], [4, 4, 5, 5], [1, 10]]
	[(0: 2), (1: 7), (2: 3), (3: 10), (4: 4), (5: 4), (6: 5), (7: 5), (8: 1), (9: 10)]
 */

void main() {
    final List<Integer> list = List.of(2, 7, 3, 10, 4, 4, 5, 5, 1, 10);
    final GathererTasks<Integer> gathererTasks = new GathererTasksImpl<>();

    Stream
        .of(
            gathererTasks.counted((a, b) -> a % 2 == b % 2),
            gathererTasks.deduplicate(),
            gathererTasks.grouped(n -> n % 2 == 0),
            gathererTasks.split(n -> n % 2 == 0),
            gathererTasks.ascending(Integer::compareTo),
            gathererTasks.indexed()
        )
        .map(gatherer -> list.stream().gather(gatherer).toList())
        .map("\t%s"::formatted)
        .forEach(System.out::println);
}
