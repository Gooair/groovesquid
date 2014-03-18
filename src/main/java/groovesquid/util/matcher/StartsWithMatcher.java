package groovesquid.util.matcher;

public class StartsWithMatcher implements SuggestMatcher {
	@Override
	public boolean matches(String dataWord, String searchWord) {
		return dataWord.startsWith(searchWord);
	}
}