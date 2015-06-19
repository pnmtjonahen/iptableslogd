package nl.tjonahen.iptableslogd.domain;

import nl.tjonahen.iptableslogd.collection.IdentityExtractor;

/**
 * Source extractor
 * @author Philippe Tjon - A - Hen, philippe@tjonahen.nl
 * @param <T> 
 */
public final class SourceExtractor<T extends LogEntry> implements IdentityExtractor<T> {

    @Override
    public String getIdentity(T t) {
        return t.getSource();
    }

}
