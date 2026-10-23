/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * The {@link TypeLocators} maintain a mapping of type names (in the form <code>p1/p2/A</code>) to code locations (in the
 * form <code>src1/p1/p2/A.java</code>) to detect duplicate type definitions in different source folders. In the case of
 * multi-release compilation, this might allow for duplicate types if they are in distinct release folders.
 */
public class TypeLocators {

	private String[] knownPackageNames; // of the form "p1/p2"

	// holds data when no release is used
	private final Map<String, String> defaultMap;

	// holds data when a release version is used:
	// 		type name -> release -> location
	private Map<String, Map<Integer, String>> releaseMap;

	TypeLocators() {
		this.defaultMap = new LinkedHashMap<>(7); //FLLDCIM: MARK — M04 M3_ORDERED_LANES NEW
	}

	TypeLocators(TypeLocators copy) {
		this.defaultMap = new LinkedHashMap<>(copy.defaultMap); //FLLDCIM: MARK — M04 M3_ORDERED_LANES NEW
		if (copy.releaseMap != null) {
			this.releaseMap = new LinkedHashMap<>(copy.releaseMap); //FLLDCIM: MARK — M04 M3_ORDERED_LANES NEW
		}
	}

	void write(CompressedWriter out, Map<String, Integer> internedTypeLocators) throws IOException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (this.defaultMap.isEmpty()) {
			out.writeInt(0);
		} else {
			out.writeInt(this.defaultMap.size());
			for (Entry<String, String> entry : this.defaultMap.entrySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				String key = entry.getKey();
				String value = entry.getValue();
				out.writeStringUsingLast(key);
				Integer index = internedTypeLocators.get(value);
				out.writeIntInRange(index.intValue(), internedTypeLocators.size());
			}
		}
		if (this.releaseMap == null || this.releaseMap.isEmpty()) {
			out.writeInt(0);
		} else {
			out.writeInt(this.releaseMap.size());
			for (var entry : this.releaseMap.entrySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				String key = entry.getKey();
				out.writeStringUsingLast(key);
				Map<Integer, String> map = entry.getValue();
				out.writeInt(map.size());
				for (var releaseEntry : map.entrySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					out.writeInt(releaseEntry.getKey());
					Integer index = internedTypeLocators.get(releaseEntry.getValue());
					out.writeIntInRange(index.intValue(), internedTypeLocators.size());
				}
			}
		}

	}

	void read(CompressedReader in, String[] internedTypeLocators) throws IOException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		int length = in.readInt();
		this.defaultMap.clear();
		if (length > 0) {
			for (int i = 0; i < length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				recordLocatorForType(in.readStringUsingLast(),
						internedTypeLocators[in.readIntInRange(internedTypeLocators.length)],
						JavaProject.NO_RELEASE);
			}
		}
		length = in.readInt();
		if (length == 0) {
			this.releaseMap = null;
		} else {
			this.releaseMap = new LinkedHashMap<>((int) (length / 0.75 + 1)); //FLLDCIM: MARK — M04 M3_ORDERED_LANES NEW
			for (int i = 0; i < length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				String key = in.readStringUsingLast();
				int mapSize = in.readInt();
				for (int j = 0; j < mapSize; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					int release = in.readInt();
					String locator = internedTypeLocators[in.readIntInRange(internedTypeLocators.length)];
					recordLocatorForType(key, locator, release);
				}
			}
		}
	}

	void removeLocator(String qualifiedTypeNameToRemove) {
		this.knownPackageNames = null;
		this.defaultMap.remove(qualifiedTypeNameToRemove);
		if (this.releaseMap != null) {
			this.releaseMap.remove(qualifiedTypeNameToRemove);
		}
	}

	void removeLocator(String typeLocatorToRemove, int release) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		this.knownPackageNames = null;
		if (release > JavaProject.NO_RELEASE) {
			if (this.releaseMap != null) {
				for (Iterator<Entry<String, Map<Integer, String>>> iterator = this.releaseMap.entrySet() //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
						.iterator(); iterator.hasNext();) {
					Entry<String, Map<Integer, String>> entry = iterator.next();
					Map<Integer, String> map = entry.getValue();
					map.values().removeIf(v -> typeLocatorToRemove.equals(v));
					if (map.isEmpty()) {
						iterator.remove();
					}
				}
			}
		} else {
			this.defaultMap.values().removeIf(v -> typeLocatorToRemove.equals(v));
		}
	}

	@SuppressWarnings("unused")
	void recordLocatorForType(String qualifiedTypeName, String typeLocator, int release) {
		this.knownPackageNames = null;
		int start = typeLocator.indexOf(qualifiedTypeName, 0);
		if (start > 0) {
			// in the common case, the qualifiedTypeName is a substring of the typeLocator so share the char[] by using
			// String.substring()
			qualifiedTypeName = typeLocator.substring(start, start + qualifiedTypeName.length());
		}
		if (release > JavaProject.NO_RELEASE) {
			if (this.releaseMap == null) {
				this.releaseMap = new LinkedHashMap<>(7); //FLLDCIM: MARK — M04 M3_ORDERED_LANES NEW
			}
			this.releaseMap.computeIfAbsent(qualifiedTypeName, nil -> new TreeMap<>()).put(release, typeLocator); //FLLDCIM: MARK — M05 M3_SORTED_LANES NEW
		} else {
			this.defaultMap.put(qualifiedTypeName, typeLocator);
		}
	}

	boolean isKnownPackage(String qualifiedPackageName) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (this.knownPackageNames == null) {
			int total = this.defaultMap.size();
			if (this.releaseMap != null) {
				total += this.releaseMap.size();
			}
			if (total == 0) {
				this.knownPackageNames = new String[0];
				return false;
			}
			LinkedHashSet<String> names = new LinkedHashSet<>(total);
			addPackages(names, this.defaultMap.keySet());
			if (this.releaseMap != null) {
				addPackages(names, this.releaseMap.keySet());
			}
			this.knownPackageNames = names.toArray(new String[names.size()]);
			Arrays.sort(this.knownPackageNames);
		}
		int result = Arrays.binarySearch(this.knownPackageNames, qualifiedPackageName);
		return result >= 0;
	}

	protected void addPackages(LinkedHashSet<String> names, Set<String> keySet) {
		for (String packageName : keySet) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			int last = packageName.lastIndexOf('/');
			packageName = last == -1 ? null : packageName.substring(0, last);
			while (packageName != null && !names.contains(packageName)) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				names.add(packageName);
				last = packageName.lastIndexOf('/');
				packageName = last == -1 ? null : packageName.substring(0, last);
			}
		}
	}

	boolean isKnownType(String qualifiedTypeName) {
		if (this.defaultMap.containsKey(qualifiedTypeName)) {
			return true;
		}
		if (this.releaseMap != null && this.releaseMap.containsKey(qualifiedTypeName)) {
			return true;
		}
		return false;
	}

	boolean isSourceFolderEmpty(IContainer sourceFolder) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String sourceFolderName = sourceFolder.getProjectRelativePath().addTrailingSeparator().toString();
		for (String value : this.defaultMap.values()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (value.startsWith(sourceFolderName)) {
				return false;
			}
		}
		if (this.releaseMap != null) {
			for (Map<Integer, String> map : this.releaseMap.values()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				for (String value : map.values()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					if (value.startsWith(sourceFolderName)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	boolean isDuplicateLocator(String qualifiedTypeName, String typeLocator, int release) {
		String string;
		if (release > JavaProject.NO_RELEASE) {
			if (this.releaseMap == null) {
				return false;
			}
			Map<Integer, String> existing = this.releaseMap.get(qualifiedTypeName);
			if (existing == null) {
				return false;
			}
			string = existing.get(release);
		} else {
			string = this.defaultMap.get(qualifiedTypeName);
		}
		return string != null && !string.equals(typeLocator);
	}

	/**
	 * This method is used in PDE API tools only!
	 *
	 * @param typeName
	 *                     the type to get all known path for
	 * @return a stream of all known path for the given type name
	 */
	public Stream<String> getPathForName(String typeName) {
		return Stream.concat(getDefaultPathForName(typeName).stream(), getReleasePathForNames(typeName)).distinct();
	}

	private Optional<String> getDefaultPathForName(String typeName) {
		return Optional.ofNullable(this.defaultMap.get(typeName));
	}

	private Stream<String> getReleasePathForNames(String typeName) {
		if (this.releaseMap == null) {
			return Stream.empty();
		} else {
			Map<Integer, String> map = this.releaseMap.get(typeName);
			if (map == null) {
				return Stream.empty();
			}
			return map.values().stream();
		}
	}

	/**
	 * Only implemented for StateTest! one usually won't use {@link TypeLocators} in a way where equals/hashCode really
	 * matters
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeLocators other) {
			if (!this.defaultMap.equals(other.defaultMap)) {
				return false;
			}
			if (!Objects.requireNonNullElse(this.releaseMap, Map.of())
					.equals(Objects.requireNonNullElse(other.releaseMap, Map.of()))) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Only implemented for StateTest! one usually won't use {@link TypeLocators} in a way where equals/hashCode really
	 * matters
	 */
	@Override
	public int hashCode() {
		return 0;
	}
}
