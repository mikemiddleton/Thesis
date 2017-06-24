package interfaces;

public interface Predicate<T> {
	boolean test(T t) throws Exception;
}
