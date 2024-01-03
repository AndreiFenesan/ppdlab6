package org.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileReader {

    public void readFileAndAddToList(List<CompetitorResult> competitorResults, String fileName, String country) {
        var filePath = Paths.get(fileName);
        try (var buff = Files.newBufferedReader(filePath)) {
            buff.lines()
                    .forEach(line -> {
                        var data = line.split(",");

                        CompetitorResult node = new CompetitorResult();
                        node.setId(Integer.parseInt(data[0]));
                        node.setScore(Integer.parseInt(data[1]));
                        node.setCountry(country);

                        competitorResults.add(node);
                    });
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
