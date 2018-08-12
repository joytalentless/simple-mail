package org.simplejavamail.internal.clisupport.model;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CliDeclaredOptionSpec implements Comparable<CliDeclaredOptionSpec> {
	@Nonnull
	private final String name;
	@Nonnull
	private final List<String> description;
	@Nonnull
	private final Collection<CliCommandType> applicableToCliCommandTypes;
	@Nonnull
	private final List<CliDeclaredOptionValue> possibleOptionValues;
	@Nonnull
	private final Method sourceMethod;
	
	public CliDeclaredOptionSpec(@Nonnull String name, @Nonnull List<String> description, @Nonnull List<CliDeclaredOptionValue> possibleArguments,
								 @Nonnull Collection<CliCommandType> applicableToCliCommandTypes, @Nonnull Method sourceMethod) {
		this.name = name;
		this.description = Collections.unmodifiableList(description);
		this.applicableToCliCommandTypes = Collections.unmodifiableCollection(applicableToCliCommandTypes);
		this.possibleOptionValues = Collections.unmodifiableList(possibleArguments);
		this.sourceMethod = sourceMethod;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean applicableToRootCommand(CliCommandType name) {
		return this.applicableToCliCommandTypes.contains(CliCommandType.all) ||
				this.applicableToCliCommandTypes.contains(name);
	}
	
	@Override
	public int compareTo(@Nonnull CliDeclaredOptionSpec other) {
		int prefixOrder = getNamePrefix().compareTo(other.getNamePrefix());
		return prefixOrder != 0 ? prefixOrder : getNameAfterPrefix().compareTo(other.getNameAfterPrefix());
	}
	
	private String getNamePrefix() {
		return getName().substring(0, getName().indexOf(":"));
	}
	
	private String getNameAfterPrefix() {
		return getName().substring(getName().indexOf(":"));
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getDescription() {
		return description;
	}
	
	public Collection<CliCommandType> getApplicableToCliCommandTypes() {
		return applicableToCliCommandTypes;
	}
	
	public List<CliDeclaredOptionValue> getPossibleOptionValues() {
		return possibleOptionValues;
	}
	
	public Method getSourceMethod() {
		return sourceMethod;
	}
}