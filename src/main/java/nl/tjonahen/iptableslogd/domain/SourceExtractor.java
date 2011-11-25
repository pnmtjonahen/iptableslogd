package nl.tjonahen.iptableslogd.domain;

import nl.tjonahen.iptableslogd.collection.IdentityExtractor;

public final class SourceExtractor<T extends LogEntry> implements IdentityExtractor<T> {

	public String getIdentity(T t) {
		return t.getSource();
	}

}
