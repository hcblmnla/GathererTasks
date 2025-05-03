import tasks.GathererTasks;
import tasks.GathererTasksImpl;

/*
Output:
	[(2, 1), (3, 2), (4, 3), (1, 3), (10, 1)]
	[[2], [7, 3], [10, 4, 4], [5, 5, 1], [10]]
	[[2], [10, 4, 4], [10]]
	[[2, 7], [3, 10], [4, 4, 5, 5], [1, 10]]
 */

void main() {
    final List<Integer> list = List.of(2, 7, 3, 10, 4, 4, 5, 5, 1, 10);
    final GathererTasks<Integer> gathererTasks = new GathererTasksImpl<>();

    Stream
        .of(
            gathererTasks.counted((a, b) -> a % 2 == b % 2),
            gathererTasks.grouped(n -> n % 2 == 0),
            gathererTasks.split(n -> n % 2 == 0),
            gathererTasks.ascending(Integer::compareTo)
        )
        .forEach(gatherer -> System.out.println(
            list.stream()
                .gather(gatherer)
                .toList()
        ));
}
