package nl.tjonahen.iptableslogd.domain;

import nl.tjonahen.iptableslogd.collection.IdentityExtractor;

public final class DestinationAndPortExtractor<T extends LogEntry> implements IdentityExtractor<T>{

	public String getIdentity(T t) {
		return t.getDestination() + t.getDestinationPort();
	}

	
}
