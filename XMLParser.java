import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;




public class XMLParser {

  final static String folderPath = "/Users/alex/Desktop/Wind-Water-Erosion-Model/wepp/operations";
  final static String WEPP_searchPattern = "<surdis>, <tdmean>";
  final static String WEPS_searchPattern = "gtdepth, gtilArea";
  static ArrayList<String> filePath = new ArrayList<>();

  public static void main(String[] args) throws Exception {

    ArrayList<String> WEPP_fileName = new ArrayList<>();
    ArrayList<String> WEPS_fileName = new ArrayList<>();
    ArrayList<String> WEPP_surdis = new ArrayList<>();
    ArrayList<String> WEPP_tdmean = new ArrayList<>();
    ArrayList<String> WEPS_gtilarea = new ArrayList<>();
    ArrayList<String> WEPS_gtdepth = new ArrayList<>();

    // Enter pattern to search
    final String pattern = "<tdmean>";

    final File folder = new File(folderPath);
    searchFile(folder, pattern, WEPP_fileName, WEPS_fileName);


    SAXParserFactory parserFactor = SAXParserFactory.newInstance();
    parserFactor.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    SAXParser parser = parserFactor.newSAXParser();
    SAXHandler handler = new SAXHandler();


    for (int i = 0; i < filePath.size(); i++) {
        parser.parse(filePath.get(i),
                     handler);
    }

    for ( Param param : handler.paramlist){
      if (param.name != null && param.name.equals("gtilArea")) {
        WEPS_gtilarea.add(param.value);
      }
      if (param.name != null && param.name.equals("gtdepth")) {
        WEPS_gtdepth.add(param.value);
      }
    }

    for (Process process : handler.processlist) {
      if (process.surdis != null && !process.surdis.isEmpty()) {
        WEPP_surdis.add(process.surdis);
      }
      if (process.tdmean != null && !process.tdmean.isEmpty()) {
        WEPP_tdmean.add(process.tdmean);
      }
    }

    //create output file.
    PrintWriter pw_WEPP = new PrintWriter("WEPP_output.txt", "UTF-8");
    pw_WEPP.println("- - - - - - - - - - - - - - - - - - - -");
    pw_WEPP.printf("%s\t%s\t%s%n", "FileName", "WEPP(surdis)", "WEPP(tdmean)");
    pw_WEPP.println("- - - - - - - - - - - - - - - - - - - -");
    for (int i = 0; i < WEPP_fileName.size(); i++) {
      pw_WEPP.printf("%s;  %s;  %s%n", WEPP_fileName.get(i), WEPP_surdis.get(i), WEPP_tdmean.get(i));
    }
    pw_WEPP.close();

    PrintWriter pw_WEPS = new PrintWriter("WEPS_output.txt", "UTF-8");
    pw_WEPS.println("- - - - - - - - - - - - - - - - - - - -");
    pw_WEPS.printf("%s\t%s\t%s%n", "FileName", "WEPS(gtilArea)", "WEPS(gtdepth)");
    pw_WEPS.println("- - - - - - - - - - - - - - - - - - - -");

    for (int i = 0; i < WEPS_fileName.size(); i++) {
      pw_WEPS.printf("%s;  %s;  %s%n", WEPS_fileName.get(i), WEPS_gtilarea.get(i), WEPS_gtdepth.get(i));
    }
    pw_WEPS.close();

  }

  public static void searchFile(final File folder, final String pattern,
                                ArrayList<String> WEPP_fileName,
                                ArrayList<String> WEPS_fileName)
                                throws FileNotFoundException {
    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
        searchFile(fileEntry, pattern, WEPP_fileName, WEPS_fileName);
      } else {

        filePath.add(fileEntry.getAbsolutePath());

        if (WEPP_searchPattern.matches("(.*)" + pattern + "(.*)")) {
          Scanner WEPP_scan = new Scanner(fileEntry);
          while (WEPP_scan.findWithinHorizon(pattern, 0) != null) {
            WEPP_fileName.add(fileEntry.getName());
          }
          WEPP_scan.close();
        }

        if (WEPS_searchPattern.matches("(.*)" + pattern + "(.*)")) {
          Scanner WEPS_scan = new Scanner(fileEntry);
          while (WEPS_scan.findWithinHorizon(pattern, 0) != null) {
            WEPS_fileName.add(fileEntry.getName());
          }
          WEPS_scan.close();
        }

      }
    }
  }


}


/**
 * The Handler for SAX Events.
 */
class SAXHandler extends DefaultHandler {

  List<Param> paramlist = new ArrayList<>();
  List<Process> processlist = new ArrayList<>();

  Param param = null;
  String content = null;
  Process process = null;

  @Override
  //Triggered when the start of tag is found.
  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
                           throws SAXException {
    switch(qName){
      //Create a new object when the start tag is found
      case "param":
        param = new Param();
        //param.id = attributes.getValue("id");
        break;
      case "process":
        process = new Process();
        break;
    }
  }

  @Override
  public void endElement(String uri, String localName,
                         String qName) throws SAXException {
   switch(qName){
     //Add to list once end tag is found
     case "param":
       paramlist.add(param);
       break;
    //For all other end tags to be updated.
     case "name":
       if (param != null) {
         param.name = content;
       }
       break;
     case "value":
       if (param != null) {
         param.value = content;
       }
       break;
    case "process":
      processlist.add(process);
      break;
     case "surdis":
       process.surdis = content;
       break;
     case "tdmean":
       process.tdmean = content;
       break;
   }
  }

  @Override
  public void characters(char[] ch, int start, int length)
          throws SAXException {
    content = String.copyValueOf(ch, start, length).trim();
  }

}

class Param {

  String name;
  String value;

  @Override
  public String toString() {
    return name + " " + value;
  }
}

class Process {
  String surdis;
  String tdmean;

  @Override
  public String toString() {
    return "Surdis: " + surdis + "\n" + "tdmean: " + tdmean;

  }
}
