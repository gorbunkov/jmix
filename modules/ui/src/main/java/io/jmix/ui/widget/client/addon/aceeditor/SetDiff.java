/*
 * Copyright 2017 Antti Nieminen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.ui.widget.client.addon.aceeditor;

import io.jmix.ui.widget.client.addon.aceeditor.AceAnnotation.MarkerAnnotation;
import io.jmix.ui.widget.client.addon.aceeditor.AceAnnotation.RowAnnotation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetDiff<V extends TransportDoc.TransportableAs<T>,T> {
	
	private final Set<V> added;
	private final Set<V> removed;

	public SetDiff(Set<V> added, Set<V> removed) {
		this.added = added;
		this.removed = removed;
	}

	public SetDiff() {
		added = Collections.emptySet();
		removed = Collections.emptySet();
	}

	public static class Differ<V extends TransportDoc.TransportableAs<T>,T> {
		public SetDiff<V,T> diff(Set<V> s1, Set<V> s2) {
			Set<V> removed = new HashSet<V>(s1);
			removed.removeAll(s2);
			
			Set<V> added = new HashSet<V>(s2);
			added.removeAll(s1);
			return new SetDiff<V,T>(added, removed);
		}
		
//		public SetDiff<V,T> fromTransport(TransportSetDiff<T> tsd) {
//			Set<V> added = new HashSet<V>();
//			for (T t : tsd.added) {
//				added.add(t.fromTransport());
//			}
//			Set<V> removed = new HashSet<V>();
//			for (T t : tsd.removed) {
//				removed.add(t.fromTransport());
//			}
//			return new SetDiff<V,T>(added, removed);
//		}
		

	}
	
	// XXX Unnecessary copy-pasting
	public static SetDiff<RowAnnotation, TransportDoc.TransportRowAnnotation> fromTransport(TransportDiff.TransportSetDiffForRowAnnotations tsd) {
		Set<RowAnnotation> added = new HashSet<RowAnnotation>();
		for (TransportDoc.TransportRowAnnotation t : tsd.added) {
			added.add(t.fromTransport());
		}
		Set<RowAnnotation> removed = new HashSet<RowAnnotation>();
		for (TransportDoc.TransportRowAnnotation t : tsd.removed) {
			removed.add(t.fromTransport());
		}
		return new SetDiff<RowAnnotation, TransportDoc.TransportRowAnnotation>(added, removed);
	}
	
	// XXX Unnecessary copy-pasting
	public static SetDiff<MarkerAnnotation, TransportDoc.TransportMarkerAnnotation> fromTransport(TransportDiff.TransportSetDiffForMarkerAnnotations tsd) {
		Set<MarkerAnnotation> added = new HashSet<MarkerAnnotation>();
		for (TransportDoc.TransportMarkerAnnotation t : tsd.added) {
			added.add(t.fromTransport());
		}
		Set<MarkerAnnotation> removed = new HashSet<MarkerAnnotation>();
		for (TransportDoc.TransportMarkerAnnotation t : tsd.removed) {
			removed.add(t.fromTransport());
		}
		return new SetDiff<MarkerAnnotation, TransportDoc.TransportMarkerAnnotation>(added, removed);
	}
	
	public Set<V> applyTo(Set<V> s1) {
		Set<V> s2 = new HashSet<V>(s1);
		s2.removeAll(removed);
		s2.addAll(added);
		return s2;
	}
	
//	public TransportSetDiff<T> asTransport() {
//		HashSet<T> ta = new HashSet<T>();
//		for (V v : added) {
//			ta.add(v.asTransport());
//		}
//		HashSet<T> tr = new HashSet<T>();
//		for (V v : removed) {
//			tr.add(v.asTransport());
//		}
//		return new TransportSetDiff<T>(ta, tr);
//	}
	
	// XXX Unnecessary copy-pasting
	public TransportDiff.TransportSetDiffForRowAnnotations asTransportRowAnnotations() {
		HashSet<TransportDoc.TransportRowAnnotation> ta = new HashSet<TransportDoc.TransportRowAnnotation>();
		for (V v : added) {
			ta.add((TransportDoc.TransportRowAnnotation) v.asTransport());
		}
		HashSet<TransportDoc.TransportRowAnnotation> tr = new HashSet<TransportDoc.TransportRowAnnotation>();
		for (V v : removed) {
			tr.add((TransportDoc.TransportRowAnnotation) v.asTransport());
		}
		return new TransportDiff.TransportSetDiffForRowAnnotations(ta, tr);
	}
	
	// XXX Unnecessary copy-pasting
	public TransportDiff.TransportSetDiffForMarkerAnnotations asTransportMarkerAnnotations() {
		HashSet<TransportDoc.TransportMarkerAnnotation> ta = new HashSet<TransportDoc.TransportMarkerAnnotation>();
		for (V v : added) {
			ta.add((TransportDoc.TransportMarkerAnnotation) v.asTransport());
		}
		HashSet<TransportDoc.TransportMarkerAnnotation> tr = new HashSet<TransportDoc.TransportMarkerAnnotation>();
		for (V v : removed) {
			tr.add((TransportDoc.TransportMarkerAnnotation) v.asTransport());
		}
		return new TransportDiff.TransportSetDiffForMarkerAnnotations(ta, tr);
	}
	
	@Override
	public String toString() {
		return "added: " + added + ", removed: " + removed;
	}
}
