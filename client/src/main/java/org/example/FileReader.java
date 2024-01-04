package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class FileReader {

    public void readFileAndAddToList(List<CompetitorResult> competitorResults, String fileName, String country) {

        File fileObj = new File(fileName);
        try(Scanner scanner = new Scanner(fileObj)){

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = line.replace("\n", "");
                var data = line.split(",");

                CompetitorResult node = new CompetitorResult();
                node.setId(Integer.parseInt(data[0]));
                node.setScore(Integer.parseInt(data[1]));
                node.setCountry(country);

                competitorResults.add(node);
            }
        } catch (FileNotFoundException exception) {
            System.out.println(exception);
        }
    }
}
