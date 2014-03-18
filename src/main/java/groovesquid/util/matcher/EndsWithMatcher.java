package groovesquid.util.matcher;

public class EndsWithMatcher implements SuggestMatcher {
	@Override
	public boolean matches(String dataWord, String searchWord) {
		return dataWord.endsWith(searchWord);
	}
}