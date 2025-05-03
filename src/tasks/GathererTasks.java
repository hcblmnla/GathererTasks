package tasks;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Gatherer;

/// A couple of classic [Gatherer] tasks for operations with arrays.
///
/// @author alnmlbch
public interface GathererTasks<T> {

    /// Groups elements satisfying bi-predicate into [Counted] pairs. `O(1)` memory.
    Gatherer<T, ?, Counted<T>> counted(BiPredicate<? super T, ? super T> biPredicate);

    /// Groups elements for which predicate returns the same result.
    Gatherer<T, ?, List<T>> grouped(Predicate<? super T> predicate);

    /// Splits elements into lists by predicate.
    Gatherer<T, ?, List<T>> split(Predicate<? super T> predicate);

    /// Groups ascending elements into lists by comparator.
    Gatherer<T, ?, List<T>> ascending(Comparator<? super T> comparator);

    /// Counted element pair.
    record Counted<T>(T value, int count) {

        @Override
        public String toString() {
            return "(%s, %d)".formatted(value, count);
        }
    }
}
