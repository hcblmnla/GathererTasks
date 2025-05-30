/*
Output:
	Hello: Hello: 1, 22, 333, Hello: 4444, 55555, 666666, Hello: 7777777, !
 */

final int chunkSize = 3;

<T, A, R> R parallel(
    final List<? extends T> values,
    final ExecutorService executor,
    final Supplier<A> identity,
    final BiFunction<A, ? super T, A> accumulator,
    final BinaryOperator<A> combiner,
    final Function<? super A, ? extends R> finalizer
) {
    final List<CompletableFuture<A>> batches = values.stream()
        .gather(Gatherers.windowFixed(chunkSize))
        .map(
            group -> CompletableFuture.supplyAsync(
                () -> group.stream().reduce(identity.get(), accumulator, combiner),
                executor
            )
        )
        .toList();
    return batches.stream()
        .map(CompletableFuture::join)
        .gather(Gatherers.fold(identity, combiner))
        .map(finalizer)
        .toList().getFirst();
}

void main() {
    System.out.println(
        parallel(
            List.of(1, 2, 3, 4, 5, 6, 7),
            Executors.newVirtualThreadPerTaskExecutor(),
            () -> "Hello: ",
            (String s, Integer n) -> s + "%d".formatted(n).repeat(n) + ", ",
            String::concat,
            (String s) -> "\t%s!".formatted(s)
        )
    );
}
