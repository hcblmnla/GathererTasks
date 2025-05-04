package element;

/// Useful element pairs.
///
/// @author alnmlbch
public interface Element {

    /// Counted element pair.
    record Counted<T>(T value, int count) {

        @Override
        public String toString() {
            return "(%d times %s)".formatted(count, value);
        }
    }

    /// Indexed element pair.
    record Indexed<T>(T value, int index) {

        @Override
        public String toString() {
            return "(%d: %s)".formatted(index, value);
        }
    }
}
