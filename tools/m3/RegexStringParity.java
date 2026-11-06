/*
 * SPDX-License-Identifier: EPL-2.0
 * Differential qualification of two mechanically applied M3 regex/string atoms.
 * Standalone, Java 21, no Eclipse dependencies or external fixtures.
 */
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexStringParity {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    private static final char[] CHARS = {' ', 'a', 'b', '%', '\t', '\n', '\r', 'é', '\u2603', '1', '_', '\uD83D', '\uDE03'};

    private RegexStringParity() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("repository root required");
        Path root = Path.of(args[0]);
        String ide = Files.readString(root.resolve(
            "org.eclipse.jdt.apt.pluggable.core/src/org/eclipse/jdt/internal/apt/pluggable/core/dispatch/IdeProcessingEnvImpl.java"));
        String javadoc = Files.readString(root.resolve(
            "org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/JavadocContents17.java"));
        if (!ide.contains("PLACEHOLDER_PATTERN.matcher(optionValue)")
            || !ide.contains("private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(\"%([^%]+)%\")")
            || !javadoc.contains("anchor = anchor.replace(\" \", \"\");")
            || !ide.contains("FLLDCIM: MARK")
            || !javadoc.contains("FLLDCIM: MARK")) {
            throw new AssertionError("source, marker or hoist changed");
        }
        Random random = new Random(0xA33F331L);
        int matched = 0;
        for (int i = 0; i < 100_000; i++) {
            StringBuilder source = new StringBuilder();
            int size = random.nextInt(70);
            for (int j = 0; j < size; j++) {
                source.append(CHARS[random.nextInt(CHARS.length)]);
            }
            String text = source.toString();
            if (!text.replaceAll(" ", "").equals(text.replace(" ", ""))) {
                throw new AssertionError("literal replace mismatch: " + i);
            }
            Matcher before = Pattern.compile("%([^%]+)%").matcher(text);
            Matcher after = PLACEHOLDER_PATTERN.matcher(text);
            boolean beforeFound = before.find();
            boolean afterFound = after.find();
            if (beforeFound != afterFound) {
                throw new AssertionError("pattern outcome mismatch: " + i);
            }
            if (beforeFound) {
                if (before.start() != after.start() || before.end() != after.end()
                    || !before.group(1).equals(after.group(1))) {
                    throw new AssertionError("pattern capture mismatch: " + i);
                }
                matched++;
            }
        }
        System.out.println("PASS Java21: 100000 differential strings, "
                + matched + " placeholder matches; source markers retained");
    }
}
