package nl.tjonahen.iptableslogd.collection;

public interface IdentityExtractor<T> {
	String getIdentity(T t);
}
