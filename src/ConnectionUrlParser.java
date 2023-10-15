public class ConnectionUrlParser {
    public static class Pair<T1, T2> {
        private final T1 first;
        private T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }

        public T1 getFirst() {
            return first;
        }

        public T2 getSecond() {
            return second;
        }

        public void changeSecond(T2 value){
            this.second = value;
        }
    }
}
