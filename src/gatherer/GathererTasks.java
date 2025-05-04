package gatherer;

import element.Element;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/// A couple of classic [Gatherer] tasks for operations with arrays.
///
/// @author alnmlbch
public interface GathererTasks<T> {

    /// Groups elements satisfying bi-predicate into [Element.Counted] pairs.
    ///
    /// @apiNote `O(1)` memory.
    Gatherer<T, ?, Element.Counted<T>> counted(BiPredicate<? super T, ? super T> biPredicate);

    /// Removes duplicates, but does not make `distinct`.
    default Gatherer<T, ?, T> deduplicate() {
        return counted(Objects::equals)
            .andThen(Gatherer.ofSequential((_, counted, downstream) -> downstream.push(counted.value())));
    }

    /// Groups elements for which predicate returns the same result.
    Gatherer<T, ?, List<T>> grouped(Predicate<? super T> predicate);

    /// Splits elements into lists by predicate.
    Gatherer<T, ?, List<T>> split(Predicate<? super T> predicate);

    /// Groups ascending elements into lists by comparator.
    Gatherer<T, ?, List<T>> ascending(Comparator<? super T> comparator);

    /// Groups elements into [Element.Indexed] pairs.
    ///
    /// @see java.util.stream.Gatherers#scan(Supplier, BiFunction)
    Gatherer<T, ?, Element.Indexed<T>> indexed();
}
