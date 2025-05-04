package gatherer;

import element.Element;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;

/// Quick-and-dirty [GathererTasks] implementation.
///
/// @author alnmlbch
public class GathererTasksImpl<T> implements GathererTasks<T> {

    @Override
    public Gatherer<T, ?, Element.Counted<T>> counted(final BiPredicate<? super T, ? super T> biPredicate) {
        Objects.requireNonNull(biPredicate, "biPredicate");

        class Counter {
            T last = null;
            int count = 0;

            boolean integrate(
                final T element,
                final Gatherer.Downstream<? super Element.Counted<T>> downstream
            ) {
                if (last == null || biPredicate.test(last, element)) {
                    last = element;
                    count++;
                    return true;
                }
                final boolean pushed = downstream.push(new Element.Counted<>(last, count));
                last = element;
                count = 1;
                return pushed;
            }

            void finish(final Gatherer.Downstream<? super Element.Counted<T>> downstream) {
                if (last != null) {
                    downstream.push(new Element.Counted<>(last, count));
                }
            }
        }

        return Gatherer.<T, Counter, Element.Counted<T>>ofSequential(
            Counter::new,
            Gatherer.Integrator.<Counter, T, Element.Counted<T>>ofGreedy(Counter::integrate),
            Counter::finish
        );
    }

    @Override
    public Gatherer<T, ?, List<T>> grouped(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");

        class Grouper extends Accumulated<T> {
            boolean last;

            @Override
            boolean integrate(final T element, final Gatherer.Downstream<? super List<T>> downstream) {
                if (acc.isEmpty()) {
                    acc.add(element);
                    last = predicate.test(element);
                    return true;
                }
                final boolean now = predicate.test(element);
                if (last == now) {
                    acc.add(element);
                    return true;
                }
                last = now;
                final List<T> copy = List.copyOf(acc);
                acc.clear();
                acc.add(element);
                return downstream.push(copy);
            }
        }

        return Accumulated.asGatherer(Grouper::new);
    }

    @Override
    public Gatherer<T, ?, List<T>> split(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");

        class Splitter extends Accumulated<T> {
            boolean integrate(final T element, final Gatherer.Downstream<? super List<T>> downstream) {
                if (predicate.test(element)) {
                    acc.add(element);
                    return true;
                }
                if (acc.isEmpty()) {
                    return true;
                }
                final List<T> copy = List.copyOf(acc);
                acc.clear();
                return downstream.push(copy);
            }
        }

        return Accumulated.asGatherer(Splitter::new);
    }

    @Override
    public Gatherer<T, ?, List<T>> ascending(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator");

        class Ascended extends Accumulated<T> {
            boolean integrate(final T element, final Gatherer.Downstream<? super List<T>> downstream) {
                if (acc.isEmpty() || comparator.compare(acc.getLast(), element) <= 0) {
                    acc.add(element);
                    return true;
                }
                final List<T> copy = List.copyOf(acc);
                acc.clear();
                acc.add(element);
                return downstream.push(copy);
            }
        }

        return Accumulated.asGatherer(Ascended::new);
    }

    @Override
    public Gatherer<T, ?, Element.Indexed<T>> indexed() {
        return Gatherers.scan(
            () -> new Element.Indexed<>(null, -1),
            (last, element) -> new Element.Indexed<>(element, last.index() + 1)
        );
    }

    private abstract static class Accumulated<T> {
        final List<T> acc = new ArrayList<>();

        static <T> Gatherer<T, ?, List<T>> asGatherer(final Supplier<Accumulated<T>> supplier) {
            return Gatherer.ofSequential(
                supplier,
                Gatherer.Integrator.ofGreedy(Accumulated::integrate),
                Accumulated::finish
            );
        }

        abstract boolean integrate(T element, Gatherer.Downstream<? super List<T>> downstream);

        void finish(final Gatherer.Downstream<? super List<T>> downstream) {
            if (!acc.isEmpty()) {
                downstream.push(acc);
            }
        }
    }
}
