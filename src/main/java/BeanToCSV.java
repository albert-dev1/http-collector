import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BeanToCSV {
    public static void write(List<WebElement> webElements) throws IOException {

        // name of generated csv 
        final String CSV_LOCATION = "reports/WebElements.csv ";
        File file = new File(CSV_LOCATION);
        file.getParentFile().mkdirs();
        file.createNewFile();
        try(FileWriter writer = new FileWriter(CSV_LOCATION)) {

            // Create Mapping Strategy to arrange the  
            // column name in order 
            ColumnPositionMappingStrategy mappingStrategy=
                    new ColumnPositionMappingStrategy();
            mappingStrategy.setType(WebElement.class);

            // Arrange column name as provided in below array. 
            String[] columns = new String[]
                    { "url", "commited", "references", "referenced" };
            mappingStrategy.setColumnMapping(columns);

            // Createing StatefulBeanToCsv object 
            StatefulBeanToCsvBuilder<WebElement> builder=
                    new StatefulBeanToCsvBuilder(writer);
            StatefulBeanToCsv beanWriter =
                    builder.withMappingStrategy(mappingStrategy).build();

            // Write list to StatefulBeanToCsv object 
            beanWriter.write(webElements);

            // closing the writer object 
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
} 