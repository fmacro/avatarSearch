package top.avatarsearch.example;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 *
 * @author fmacro
 * @date 2020/5/27 16:10
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ImagepHash imagepHash = new ImagepHash();

        InputStream inputStream = new ClassPathResource("static/img/screenshots.png").getInputStream();

        String pathTemp = "static/img/%d.jpg";
        String hash1 = imagepHash.getHash(inputStream);
        for (int i = 1; i <5 ; i++) {
            String path = String.format(pathTemp,i);
            InputStream inputStream1 = new ClassPathResource(path).getInputStream();
            String hash2 = imagepHash.getHash(inputStream1);
            if(imagepHash.distance(hash1,hash2)>80){
                System.out.println(path);
            }
        }
    }
}
