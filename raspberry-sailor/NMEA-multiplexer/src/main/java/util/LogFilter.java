package util;

// import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Small utility, to remove/filter some sentences,
 * based on talker and/or sentence IDs.
 */
public class LogFilter {

	private final static String HELP_PREFIX = "--help";
	private final static String INPUT_LOG_FILE_PREFIX = "--input-data-file:";
	private final static String OUTPUT_LOG_FILE_PREFIX = "--output-data-file:";
	private final static String INCLUDE_TALKER_PREFIX = "--include-talkers:";
	private final static String EXCLUDE_TALKER_PREFIX = "--exclude-talkers:";
	private final static String INCLUDE_SENTENCE_PREFIX = "--include-sentences:";
	private final static String EXCLUDE_SENTENCE_PREFIX = "--exclude-sentences:";

	private static void help() {
		System.out.printf("CLI parameters are:\n%s\n%s<CSV>\n%s<CSV>\n%s<CSV>\n%s<CSV>\n%s<CSV>\n%s<CSV>\n",
				HELP_PREFIX,
				INPUT_LOG_FILE_PREFIX,
				OUTPUT_LOG_FILE_PREFIX,
				INCLUDE_TALKER_PREFIX,
				EXCLUDE_SENTENCE_PREFIX,
				INCLUDE_SENTENCE_PREFIX,
				EXCLUDE_SENTENCE_PREFIX);
		System.out.println("CVS is a Comma Separated Value List, like 'II,GP', or 'RMC,GLL,GGA'.");
	}
	public static void main(String... args) {
		String inputFileName = null;
		String outputFileName = null;
		List<String> excludeTalkers = new ArrayList<>();
		List<String> includeTalkers = new ArrayList<>();
		List<String> excludeSentences = new ArrayList<>();
		List<String> includeSentences = new ArrayList<>();

		if (args.length > 0) {
			for (String arg : args) {
				if (arg.startsWith(INPUT_LOG_FILE_PREFIX)) {
					inputFileName = arg.substring(INPUT_LOG_FILE_PREFIX.length());
				} else if (arg.startsWith(OUTPUT_LOG_FILE_PREFIX)) {
					outputFileName = arg.substring(OUTPUT_LOG_FILE_PREFIX.length());
				} else if (arg.startsWith(INCLUDE_TALKER_PREFIX)) {
					String listValue = arg.substring(INCLUDE_TALKER_PREFIX.length());
					includeTalkers = Arrays.asList(listValue.split(","));
				} else if (arg.startsWith(EXCLUDE_TALKER_PREFIX)) {
					String listValue = arg.substring(EXCLUDE_TALKER_PREFIX.length());
					excludeTalkers = Arrays.asList(listValue.split(","));
				} else if (arg.startsWith(INCLUDE_SENTENCE_PREFIX)) {
					String listValue = arg.substring(INCLUDE_SENTENCE_PREFIX.length());
					includeSentences = Arrays.asList(listValue.split(","));
				} else if (arg.startsWith(EXCLUDE_SENTENCE_PREFIX)) {
					String listValue = arg.substring(EXCLUDE_SENTENCE_PREFIX.length());
					excludeSentences = Arrays.asList(listValue.split(","));
				} else if (arg.startsWith(HELP_PREFIX)) {
					help();
					System.exit(0);
				}
			}
		}

		if (inputFileName == null || outputFileName == null) {
			System.out.printf("Please provide input AND out file names as CLI parameters (%s, %s)\n", INPUT_LOG_FILE_PREFIX, OUTPUT_LOG_FILE_PREFIX);
			System.exit(1);
		}
		if (includeTalkers.size() > 0 && excludeTalkers.size() > 0) {
			System.out.println("Include OR exclude talker IDs (XOR), not both.");
			System.exit(1);
		}
		if (includeSentences.size() > 0 && excludeSentences.size() > 0) {
			System.out.println("Include OR exclude sentence IDs (XOR), not both.");
			System.exit(1);
		}

		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFileName));
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName));
			String line;
			long lineNum = 0L;
			long writtenLineNum = 0L;
			boolean go = true;
			while (go) {
				line = bufferedReader.readLine();
				if (line == null) {
					go = false;
				} else {
					boolean valid = StringParsers.validCheckSum(line);
					if (valid) {
						String sentenceID = StringParsers.getSentenceID(line);
						String talkerId = StringParsers.getTalkerID(line);
						boolean talkerOK = true;
						boolean writeOK = true;
						if (includeTalkers.size() > 0 && !includeTalkers.contains(talkerId)) {
							talkerOK = false;
						}
						if (excludeTalkers.size() > 0 && excludeTalkers.contains(talkerId)) {
							talkerOK = false;
						}
						if (talkerOK) {
							if (includeSentences.size() > 0 && !includeSentences.contains(sentenceID)) {
								writeOK = false;
							}
							if (excludeSentences.size() > 0 && excludeSentences.contains(sentenceID)) {
								writeOK = false;
							}
							if (writeOK) {
								bufferedWriter.write(line + StringParsers.NMEA_EOS);
								writtenLineNum += 1;
							}
						}
					} else {
						System.err.printf("Invalid Checksum for [%s], line # %d\n", line, lineNum);
					}
					lineNum++;
				}
			}
			bufferedReader.close();
			bufferedWriter.close();
			System.out.printf("Read %s lines, written %s lines.\n", 
								NumberFormat.getInstance().format(lineNum), 
								NumberFormat.getInstance().format(writtenLineNum));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
