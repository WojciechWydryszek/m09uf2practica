package cat.uvic.teknos.m09.uf2;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.Scanner;

public class Program {
    private static boolean follow = true;
    private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) throws NoSuchAlgorithmException, URISyntaxException, IOException {
        var properties = new Properties();

        var hashParameters = new HashParameters(properties.getProperty("algorithm"), properties.getProperty("salt"));

        var threadVerify = new Thread(() -> {
            try {
                properties.load(new FileInputStream("build/resources/main/hash.properties"));

                while (follow) {
                    verifyPropertis(properties, hashParameters);
                    Thread.sleep(60*1000);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        properties.load(Program.class.getResourceAsStream("/hash.properties"));

        threadVerify.start();

        while (follow) {

            System.out.println("Type the path of the file you want to hash");
            System.out.println("Digest: " + getDigest(in.nextLine(), hashParameters));
            System.out.println("Salt: " + hashParameters.getSalt());

            askToFollow();
        }
        System.out.println("Bye!");
    }
    
    /* test path

        C:\Users\10030110\Desktop\final.docx
        Digest: E3e97rolPDJfmiUmaAc2cjTKXzeCKvZwLsKAcz7bU5k=
        Salt: J0rNanDPVYQBwEUxQVNaDQ==


        C:\Users\10030110\Desktop\final.docx
        Digest: XHNL2HLOk9VWXEth8+RGBQHxidy8VhzSRZSD8L/O9J4=
        Salt: yTOGiR4yQPDweCHhzJoOlQ==
    */
    private static void verifyPropertis(Properties properties, HashParameters hashParameters) throws IOException {
        properties.load(Program.class.getResourceAsStream("/hash.properties"));
        hashParameters.setAlgorithm(properties.getProperty("algorithm"));
        hashParameters.setSalt(properties.getProperty("salt"));

        var salt = properties.getProperty("salt");
    }
    private static void askToFollow() {
        System.out.println("Type 'e' to exit or just enter to follow");
        var exit = in.nextLine();

        if (exit.trim().toLowerCase().equals("e")) {
            follow = false;
        }
    }

    private static String getDigest(String path, HashParameters parameters) throws NoSuchAlgorithmException, URISyntaxException, IOException {
        String digestBase64 = null;
        var pathObj = Paths.get(path);
        if (Files.exists(pathObj)) {
            var data = Files.readAllBytes(pathObj);

            var messageDigest = MessageDigest.getInstance(parameters.getAlgorithm());
            messageDigest.update(parameters.getSaltBytes());

            var digest = messageDigest.digest(data);

            var base64Encoder = Base64.getEncoder();

            digestBase64 =  base64Encoder.encodeToString(digest);
        }

        return digestBase64;
    }


}