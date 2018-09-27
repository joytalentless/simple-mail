package org.simplejavamail.internal.clisupport.therapijavadoc;

import com.github.therapi.runtimejavadoc.Comment;
import com.github.therapi.runtimejavadoc.CommentElement;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.CommentText;
import com.github.therapi.runtimejavadoc.InlineLink;
import com.github.therapi.runtimejavadoc.Link;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;
import org.bbottema.javareflection.ClassUtils;
import org.bbottema.javareflection.MethodUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.github.therapi.runtimejavadoc.Comment.nullToEmpty;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.simplejavamail.internal.util.Preconditions.assumeTrue;

public final class TherapiJavadocHelper {
	
	private static final CommentFormatter COMMENT_FORMATTER = new CommentFormatter();
	private static final String KEY_DELEGATES_TO = "Delegates to";
	private static final Pattern PATTERN_DELEGATES_TO = compile("(?i)" + quote(KEY_DELEGATES_TO));
	
	private static final Map<List<CommentElement>, Method> CACHED_METHOD_DELEGATES = new HashMap<>();
	
	private TherapiJavadocHelper() {
	}
	
	/**
	 * @return The Method referred to by the first {@code @link} if it occurs in the original javadoc.
	 */
	@Nullable
	public static Method getTryFindMethodDelegate(@Nullable final Comment comment) {
		final List<CommentElement> commentElements = nullToEmpty(comment).getElements();
		
		if (CACHED_METHOD_DELEGATES.containsKey(commentElements)) {
			return CACHED_METHOD_DELEGATES.get(commentElements);
		}
		
		CommentElement lastEleTextSaysDelegatedTo = null;
		for (CommentElement ele : commentElements) {
			if (ele instanceof CommentText) {
				if (PATTERN_DELEGATES_TO.matcher(((CommentText) ele).getValue()).find()) {
					lastEleTextSaysDelegatedTo = ele;
				}
			} else {
				if (ele instanceof InlineLink && lastEleTextSaysDelegatedTo != null) {
					return addDelegateToCache(commentElements, findMethodForLink(((InlineLink) ele).getLink()));
				}
				
				lastEleTextSaysDelegatedTo = null;
			}
		}
		
		return null;
	}
	
	private static Method addDelegateToCache(List<CommentElement> commentElements, Method methodForLink) {
		CACHED_METHOD_DELEGATES.put(commentElements, methodForLink);
		return methodForLink;
	}
	
	@Nonnull
	private static Method findMethodForLink(Link link) {
		Class<?> aClass = ClassUtils.locateClass(link.getReferencedClassName(), "org.simplejavamail", null);
		assumeTrue(aClass != null, "Class not found for @link: " + link);
		Set<Method> matchingMethods = MethodUtils.findMatchingMethods(aClass, link.getReferencedMemberName(), link.getParams());
		assumeTrue(!matchingMethods.isEmpty(), format("Method not found on %s for @link: %s", aClass, link));
		assumeTrue(matchingMethods.size() == 1, format("Multiple methods on %s match given @link's signature: %s", aClass, link));
		return matchingMethods.iterator().next();
	}
	
	public static String getJavadoc(Method m) {
		return COMMENT_FORMATTER.format(RuntimeJavadoc.getJavadoc(m).getComment());
	}
	
	public static List<MethodParam> getParamDescriptions(Method m) {
		List<ParamJavadoc> params = RuntimeJavadoc.getJavadoc(m).getParams();
		if (m.getParameterTypes().length != params.size()) {
			throw new AssertionError("Number of documented parameters doesn't match with Method's actual parameters: " + m);
		}
		List<MethodParam> paramDescriptions = new ArrayList<>();
		for (ParamJavadoc param : params) {
			paramDescriptions.add(new MethodParam(null, param.getName(), COMMENT_FORMATTER.format(param.getComment())));
		}
		return paramDescriptions;
	}
	
	public static class MethodParam {
		@Nonnull private final Class<?> type;
		@Nonnull private final String name;
		@Nonnull private final String javadoc;
		
		public MethodParam(@Nonnull Class<?> type, @Nonnull String name, @Nonnull String javadoc) {
			this.type = type;
			this.name = name;
			this.javadoc = javadoc;
		}
		@Nonnull public Class<?> getType() { return type; }
		@Nonnull public String getName() { return name; }
		@Nonnull public String getJavadoc() { return javadoc; }
	}
}