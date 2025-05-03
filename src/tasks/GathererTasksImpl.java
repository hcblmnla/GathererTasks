package tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Downstream;
import java.util.stream.Gatherer.Integrator;

public class GathererTasksImpl<T> implements GathererTasks<T> {

    @Override
    public Gatherer<T, ?, Counted<T>> counted(final BiPredicate<? super T, ? super T> biPredicate) {
        Objects.requireNonNull(biPredicate, "biPredicate");

        class Counter {
            T last = null;
            int count = 0;

            boolean integrate(final T element, final Downstream<? super Counted<T>> downstream) {
                if (last == null || biPredicate.test(last, element)) {
                    last = element;
                    count++;
                    return true;
                }
                final Counted<T> counted = new Counted<>(last, count);
                last = element;
                count = 1;
                return downstream.push(counted);
            }

            void finish(final Downstream<? super Counted<T>> downstream) {
                if (last != null) {
                    downstream.push(new Counted<>(last, count));
                }
            }
        }

        return Gatherer.<T, Counter, Counted<T>>ofSequential(
            Counter::new,
            Integrator.<Counter, T, Counted<T>>ofGreedy(Counter::integrate),
            Counter::finish
        );
    }

    @Override
    public Gatherer<T, ?, List<T>> grouped(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");

        class Grouper {
            final List<T> accumulator = new ArrayList<>();
            boolean last;

            boolean integrate(final T element, final Downstream<? super List<T>> downstream) {
                if (accumulator.isEmpty()) {
                    accumulator.add(element);
                    last = predicate.test(element);
                    return true;
                }
                final boolean now = predicate.test(element);
                if (last == now) {
                    accumulator.add(element);
                    return true;
                }
                last = now;
                final List<T> copy = new ArrayList<>(accumulator);
                accumulator.clear();
                accumulator.add(element);
                return downstream.push(copy);
            }

            // :NOTE: copy-paste
            void finish(final Downstream<? super List<T>> downstream) {
                if (!accumulator.isEmpty()) {
                    downstream.push(accumulator);
                }
            }
        }

        return Gatherer.<T, Grouper, List<T>>ofSequential(
            Grouper::new,
            Integrator.<Grouper, T, List<T>>ofGreedy(Grouper::integrate),
            Grouper::finish
        );
    }

    @Override
    public Gatherer<T, ?, List<T>> split(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");

        class Splitter {
            final List<T> accumulator = new ArrayList<>();

            boolean integrate(final T element, final Downstream<? super List<T>> downstream) {
                if (predicate.test(element)) {
                    accumulator.add(element);
                    return true;
                }
                if (accumulator.isEmpty()) {
                    return true;
                }
                final List<T> copy = new ArrayList<>(accumulator);
                accumulator.clear();
                return downstream.push(copy);
            }

            void finish(final Downstream<? super List<T>> downstream) {
                if (!accumulator.isEmpty()) {
                    downstream.push(accumulator);
                }
            }
        }

        return Gatherer.<T, Splitter, List<T>>ofSequential(
            Splitter::new,
            Integrator.<Splitter, T, List<T>>ofGreedy(Splitter::integrate),
            Splitter::finish
        );
    }

    @Override
    public Gatherer<T, ?, List<T>> ascending(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator");

        class Ascended {
            final List<T> accumulator = new ArrayList<>();

            boolean integrate(final T element, final Downstream<? super List<T>> downstream) {
                if (accumulator.isEmpty() || comparator.compare(accumulator.getLast(), element) <= 0) {
                    accumulator.add(element);
                    return true;
                }
                final List<T> copy = new ArrayList<>(accumulator);
                accumulator.clear();
                accumulator.add(element);
                return downstream.push(copy);
            }

            void finish(final Downstream<? super List<T>> downstream) {
                if (!accumulator.isEmpty()) {
                    downstream.push(accumulator);
                }
            }
        }

        return Gatherer.<T, Ascended, List<T>>ofSequential(
            Ascended::new,
            Integrator.<Ascended, T, List<T>>ofGreedy(Ascended::integrate),
            Ascended::finish
        );
    }
}
