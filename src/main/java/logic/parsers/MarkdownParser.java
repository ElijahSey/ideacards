package logic.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import entity.Card;
import entity.CardSet;
import entity.Topic;
import javafx.stage.FileChooser.ExtensionFilter;

public class MarkdownParser extends CardParser {

	public MarkdownParser(String name) {
		super(name);
	}

	@Override
	public void read(File file, CardSet cardSet, List<Topic> topics, List<Card> cards) throws IOException {

		int maxDepth = 0;
		try (Scanner sc = new Scanner(file)) {
			maxDepth = checkDepth(sc);
		}
		final String TOPIC_PRE = "#".repeat(maxDepth - 1) + " ";
		final String CARD_PRE = "#".repeat(maxDepth) + " ";

		String text = Files.readString(file.toPath());

		String[] topicArr = Pattern.compile("^" + TOPIC_PRE, Pattern.MULTILINE).split(text);
		for (int i = 1; i < topicArr.length; i++) {
			String topicString = topicArr[i];
			String[] topicSplit = Pattern.compile("\\R").split(topicString, 2);
			String[] cardArr = Pattern.compile("^" + CARD_PRE, Pattern.MULTILINE).split(topicSplit[1].trim());
			Topic topic = new Topic(topicSplit[0], cardSet);
			topics.add(topic);
			for (int k = 1; k < cardArr.length; k++) {
				String cardString = cardArr[k];
				String[] cardSplit = Pattern.compile("\\R").split(cardString, 2);
				cards.add(new Card(topic, cardSplit[0], cardSplit[1], ""));
			}
		}
	}

	@Override
	public void write(File file, CardSet cardSet, Map<String, List<Card>> cards) throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write("# " + cardSet.getName());
			Set<String> topics = cards.keySet();
			for (String topic : topics) {
				writer.newLine();
				writer.newLine();
				writer.write("## " + topic);
				for (Card card : cards.get(topic)) {
					writer.newLine();
					writer.newLine();
					writer.write("### " + card.getQuestion());
					writer.newLine();
					writer.newLine();
					writer.write(card.getSolution());
				}
			}
			writer.flush();
		}
	}

	private int checkDepth(Scanner sc) {

		int hashtags = -1;

		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.startsWith("#")) {
				if (hashtags < line.indexOf(" ")) {
					hashtags = line.indexOf(" ");
				}
			}
		}
		return hashtags;
	}

	@Override
	public ExtensionFilter createFileFilter() {
		return new ExtensionFilter("Markdown Files (.md)", "md");
	}

	@Override
	public CardParser newInstance() {
		return new MarkdownParser("");
	}
}
