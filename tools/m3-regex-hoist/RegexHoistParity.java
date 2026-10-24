// SPDX-License-Identifier: EPL-2.0
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Standalone Java 21 differential witness for the source-compatible regex-hoist recipe.
 * Run with: java tools/m3-regex-hoist/RegexHoistParity.java
 */
public final class RegexHoistParity {
    private static final Pattern TOKENS = Pattern.compile("\\s");
    private static final Pattern SEGMENTS = Pattern.compile("[.]");
    private static final Pattern BULLET = Pattern.compile("[-+*]");
    private static final Pattern MARKDOWN_LIST = Pattern.compile("\\d+[.)]");
    private static final Pattern SNIPPET = Pattern.compile("['\\\"]?java['\\\"]?");
    private static final Pattern GETTER = Pattern.compile("(is|get)\\p{Lu}.*");
    private static final Pattern SETTER = Pattern.compile("set\\p{Lu}.*");
    private static final Pattern CATEGORIES = Pattern.compile("\\s+");
    private static final Pattern PATCH_MODULES = Pattern.compile("::");
    private static int checks;

    private RegexHoistParity() {}

    public static void main(String[] args) {
        final String[] fixed = {
            "", "a", "a.b", ".a.", "..", "::", "a::b::",
            "a b", " a ", "a\tb", "x\ny", "  ",
            "-", "+", "*", "4)", "98.", "java", "'java'", "\"java\"",
            "getFoo", "isX", "setBar", "getfoo", "setÉcole",
            "देवनागरी", "日本語", "\uD83D\uDE00"
        };
        for (String value : fixed) {
            parity(value);
        }
        final Random random = new Random(0x5EEDC0DEL);
        final String alphabet = " a.b:+*()-_\t\n0123456789javaGETSet";
        for (int iteration = 0; iteration < 10_000; iteration++) {
            final int length = random.nextInt(64);
            final StringBuilder candidate = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                candidate.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            parity(candidate.toString());
        }
        System.out.println("PASS regex-hoist differential checks=" + checks);
    }

    private static void parity(String value) {
        same(Arrays.equals(value.split("\\s", 2), TOKENS.split(value, 2)), "TOKENS");
        same(Arrays.equals(value.split("[.]"), SEGMENTS.split(value)), "SEGMENTS");
        same(value.matches("[-+*]") == BULLET.matcher(value).matches(), "BULLET");
        same(value.matches("\\d+[.)]") == MARKDOWN_LIST.matcher(value).matches(), "MARKDOWN_LIST");
        same(value.matches("['\\\"]?java['\\\"]?")
                == SNIPPET.matcher(value).matches(), "SNIPPET");
        same(value.matches("(is|get)\\p{Lu}.*") == GETTER.matcher(value).matches(), "GETTER");
        same(value.matches("set\\p{Lu}.*") == SETTER.matcher(value).matches(), "SETTER");
        same(Arrays.equals(value.split("\\s+"), CATEGORIES.split(value)), "CATEGORIES");
        same(Arrays.equals(value.split("::"), PATCH_MODULES.split(value)), "PATCH_MODULES");
        same(value.replaceAll(" ", "").equals(value.replace(" ", "")), "NON_REGEX_REPLACE");
    }

    private static void same(boolean result, String operation) {
        checks++;
        if (!result) throw new AssertionError(operation + " failed at check=" + checks);
    }
}
