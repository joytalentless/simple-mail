package org.simplejavamail.internal.clisupport.therapijavadoc;

import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.InlineLink;
import com.github.therapi.runtimejavadoc.Link;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;
import com.github.therapi.runtimejavadoc.SeeAlsoJavadoc;
import com.github.therapi.runtimejavadoc.Value;
import org.bbottema.javareflection.ClassUtils;
import org.bbottema.javareflection.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.simplejavamail.internal.clisupport.BuilderApiToPicocliCommandsMapper.colorizeDescriptions;
import static org.simplejavamail.internal.util.ListUtil.getFirst;
import static org.simplejavamail.internal.util.StringUtil.padRight;

public final class TherapiJavadocHelper {

	private static final Pattern LEGACY_PARAMETER_TYPE_PATTERN = compile("(?:.*:: )?([\\w.]+)\\)?");
	
	private TherapiJavadocHelper() {
	}
	
	@Nullable
	static Method findMethodForLink(Link link) {
		if (link.getReferencedMemberName() != null) {
			final Class<?> aClass = findReferencedClass(link.getReferencedClassName());
			if (aClass != null) { // else assume the class is irrelevant to CLI
				final Set<Method> matchingMethods = MethodUtils.findMatchingMethods(aClass, aClass, link.getReferencedMemberName(), link.getParams());
				return matchingMethods.isEmpty() ? null : matchingMethods.iterator().next();
			}
		}
		return null;
	}

	@Nullable
	static Object resolveFieldForValue(Value value) {
		final Class<?> aClass = findReferencedClass(value.getReferencedClassName());
		if (aClass != null) { // else assume the class is irrelevant to CLI
			return ClassUtils.solveFieldValue(aClass, value.getReferencedMemberName());
		}
		return null;
	}

	@Nullable
	private static Class<?> findReferencedClass(String referencedClassName) {
		Class<?> aClass = ClassUtils.locateClass(referencedClassName, "org.simplejavamail", null);
		return aClass != null ? aClass : ClassUtils.locateClass(referencedClassName, null, null);
	}

	@NotNull
	static String getJavadocMainDescription(Method m, int nestingDepth) {
		return new JavadocForCliFormatter(nestingDepth)
				.format(getMethodJavadoc(m).getComment());
	}
	
	@NotNull
	public static List<DocumentedMethodParam> getParamDescriptions(Method m) {
		List<ParamJavadoc> params = getMethodJavadoc(m).getParams();
		if (m.getParameterTypes().length != params.size()) {
			throw new AssertionError("Number of documented parameters doesn't match with Method's actual parameters: " + m);
		}
		List<DocumentedMethodParam> paramDescriptions = new ArrayList<>();
		for (ParamJavadoc param : params) {
			paramDescriptions.add(new DocumentedMethodParam(param.getName(), new JavadocForCliFormatter().format(param.getComment())));
		}
		return paramDescriptions;
	}

	@NotNull
	public static List<String> getJavadocSeeAlsoReferences(Method m, boolean onlyIncludeClicompatibleJavadocLinks, int maxTextWidth) {
		List<String> seeAlsoReferences = new ArrayList<>();
		final JavadocForCliFormatter cliFormatter = new JavadocForCliFormatter();
		
		// javadoc links are aligned: descriptions are leftpadded to the longest javadoc link or all moved to new lines
		int longestLink = 0;
		boolean allDescriptionsOnNextLine = false;
		
		for (SeeAlsoJavadoc seeAlsoJavadoc : getMethodJavadoc(m).getSeeAlso()) {
			switch (seeAlsoJavadoc.getSeeAlsoType()) {
				case STRING_LITERAL:
					seeAlsoReferences.add(seeAlsoJavadoc.getStringLiteral());
					break;
				case HTML_LINK:
					SeeAlsoJavadoc.HtmlLink htmlLink = seeAlsoJavadoc.getHtmlLink();
					seeAlsoReferences.add(format("%s (%s)", htmlLink.getText(), htmlLink.getLink()));
					break;
				case JAVADOC_LINK:
					final String renderedLink = cliFormatter.renderLink(new InlineLink(seeAlsoJavadoc.getLink()), false);
					if (!onlyIncludeClicompatibleJavadocLinks || renderedLink.contains("--")) { // -- -> cli compatible
						final Method linkedMethod = findMethodForLink(seeAlsoJavadoc.getLink());
						if (linkedMethod != null) {
							final List<String> fullDescription = determineCliOptionDescriptions(linkedMethod);
							final String moreInfix = fullDescription.size() > 1 ? " (...more)" : "";
							String fullSeeAlsoLine = format("[[%s]] - %s %s", renderedLink, getFirst(fullDescription), moreInfix);
							seeAlsoReferences.add(fullSeeAlsoLine);
							allDescriptionsOnNextLine |= fullSeeAlsoLine.length() > maxTextWidth;
							longestLink = Math.max(longestLink, renderedLink.length()); // keep track of padding needed lateron
						} else {
							seeAlsoReferences.add(renderedLink);
						}
					}
					break;
			}
		}
		
		// add padding for readability
		if (longestLink > 0) {
			for (int i = 0; i < seeAlsoReferences.size(); i++) {
				Matcher matcher = compile("\\[\\[(?<link>.+?)]](?<description>.*)").matcher(seeAlsoReferences.get(i));
				if (matcher.find()) {
					String newlineFixer = seeAlsoReferences.size() > 1 && allDescriptionsOnNextLine ? "\n\t" : "";
					String paddedReplacement = padRight(matcher.group("link"), longestLink) + newlineFixer + matcher.group("description");
					seeAlsoReferences.set(i, matcher.replaceFirst(paddedReplacement));
				}
			}
		}
		
		return seeAlsoReferences;
	}

	/**
	 * @deprecated this is a workaround for https://github.com/dnault/therapi-runtime-javadoc/issues/50
	 */
	@Deprecated
	private static MethodJavadoc getMethodJavadoc(final Method m) {
		ClassJavadoc javadoc = RuntimeJavadoc.getJavadoc(m.getDeclaringClass());
		for (MethodJavadoc methodJavadoc : javadoc.getMethods()) {
			if (matches(m, methodJavadoc)) {
				return methodJavadoc;
			}
		}
		return MethodJavadoc.createEmpty(m);
	}

	/**
	 * @deprecated this is a workaround for https://github.com/dnault/therapi-runtime-javadoc/issues/50
	 */
	@Deprecated
	private static boolean matches(final Method m, final MethodJavadoc methodJavadoc) {
		if (!m.getName().equals(methodJavadoc.getName())) {
			return false;
		}
		List<String> methodParamsTypes = new ArrayList<>();
		for (Class<?> aClass : m.getParameterTypes()) {
			methodParamsTypes.add(aClass.getCanonicalName());
		}
		// FIX (deprecated)
		List<String> paramTypesStripped = methodJavadoc.getParamTypes().stream()
				.map(s -> LEGACY_PARAMETER_TYPE_PATTERN.matcher(s).replaceFirst("$1"))
				.collect(toList());
		// /FIX
		return methodParamsTypes.equals(paramTypesStripped);
	}

	@NotNull
	public static List<String> determineCliOptionDescriptions(Method m) {
		String javadoc = TherapiJavadocHelper.getJavadocMainDescription(m, 0);
		// Picocli takes the first item for --help, but all items for full usage display
		List<String> basicExplanationPlusFurtherDetails = asList(javadoc.split("\n", 2));
		return colorizeDescriptions(basicExplanationPlusFurtherDetails);
	}
	
	public static class DocumentedMethodParam {
		@NotNull private final String name;
		@NotNull private final String javadoc;
		
		DocumentedMethodParam(@NotNull String name, @NotNull String javadoc) {
			this.name = name;
			this.javadoc = javadoc;
		}
		@NotNull public String getName() { return name; }
		@NotNull public String getJavadoc() { return javadoc; }
	}
}