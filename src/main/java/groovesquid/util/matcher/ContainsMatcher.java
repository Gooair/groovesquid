package groovesquid.util.matcher;

public class ContainsMatcher implements SuggestMatcher {
	@Override
	public boolean matches(String dataWord, String searchWord) {
		return dataWord.contains(searchWord);
	}
}