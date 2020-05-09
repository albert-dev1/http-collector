import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

@Builder
@NoArgsConstructor
public class FilesystemCommitter implements Committer {

    @Override
    public void commit(URL url, byte[] payload, String fileEnding) {

        String path = url.getPath();
        String file ="";
        String dir = "";

        if (path.contains("/")) {
            int i = path.lastIndexOf("/");
            file = path.contains(".") ? path.substring(i+1) : path.substring(i+1) + fileEnding;
            dir = "site/" + path.substring(0,i);
        } else  {
            dir = "site";
            file = path + fileEnding;
        }

        File targetFile = new File(new File(System.getProperty("user.dir") + "/" + dir), file);
        targetFile.getParentFile().mkdirs();

        writeToFilesystem(payload, targetFile);


    }



    private void writeToFilesystem(byte[] html, File targetFile) {
        try(FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
