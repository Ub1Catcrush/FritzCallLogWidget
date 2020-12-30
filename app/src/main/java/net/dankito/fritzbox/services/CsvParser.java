package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 26/11/16.
 */

public class CsvParser implements ICsvParser {

  private static final String CALL_LIST_CSV_HEADER = "sep=;\nTyp;Datum;Name;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer\n";

  private static final Logger log = LoggerFactory.getLogger(CsvParser.class);


  @Override
  public boolean isCallListCsv(String response) {
    return StringUtils.isNotNullOrEmpty(response) && response.startsWith(CALL_LIST_CSV_HEADER);
  }

  /**
   * Parses a call list CSV file.
   *
   * @param csvString
   *            The CSV file as string.
   * @return The parsed calls as list of Call objects.
   */
  @Override
  public List<Call> parseCallList(final String csvString) {
    final List<Call> callerList = new ArrayList<>();

    String csv = csvString.substring(csvString.indexOf("\n") + 1); // remove sep=;
    csv = csv.substring(csv.indexOf("\n") + 1); // remove Csv header
    csv = new String(csv.getBytes(), Charset.forName("UTF-8"));

    String line;
    while (csv.indexOf("\n") > 0 && (line = csv.substring(0, csv.indexOf("\n"))).length() > 6) {
      csv = csv.substring(csv.indexOf("\n") + 1);
      callerList.add(parseCallListEntry(line));
    }

    return callerList;
  }

  /**
   * Parses a certain line of the call list CSV file.
   *
   * @param csvLine
   *            The line of the CSV call list file.
   * @return The parsed call.
   */
  protected Call parseCallListEntry(final String csvLine) {
    final String[] lineData = csvLine.split(";");

    final Call.Builder builder = new Call.Builder()
        .type(lineData[0])
        .callerName(lineData[2])
        .callerNumber(lineData[3])
        .substationName(lineData[4])
        .substationNumber(lineData[5]);

    try {
      builder.date(lineData[1]);
    } catch (final ParseException e) {
      log.error("Could not parse Date from " + lineData[1], e);
    }

    try {
      builder.duration(lineData[6]);
    } catch (final ParseException e) {
      log.error("Could not parse Duration from " + lineData[6], e);
    }

    return builder.build();
  }
}
