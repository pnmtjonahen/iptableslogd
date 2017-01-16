package nl.tjonahen.iptableslogd.collection;

/**
 * Identity extractor, used in the collections to get the identity (aka primairy key) of a collection element.
 *
 * @author Philippe Tjon - A - Hen, philippe@tjonahen.nl
 * @param <T>
 */
@FunctionalInterface
public interface IdentityExtractor<T> {
    /**
     * 
     * @param type the collection element
     * @return the identity
     */
    String getIdentity(T type);
}
