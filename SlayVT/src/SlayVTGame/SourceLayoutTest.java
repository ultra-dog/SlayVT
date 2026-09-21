package SlayVTGame;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Checks that the Eclipse source tree contains only the Java game.
 * Run with the absolute path to the Eclipse project's src directory.
 */
public class SourceLayoutTest
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            throw new IllegalArgumentException("Provide the project's src path.");
        }
        Path source = Paths.get(args[0]).toRealPath();
        Path game = source.resolve("SlayVTGame");
        if (!Files.isDirectory(game))
        {
            throw new AssertionError("Missing SlayVTGame package: " + game);
        }
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(source))
        {
            for (Path entry : entries)
            {
                if (!entry.equals(game))
                {
                    throw new AssertionError("Unexpected source entry: " + entry);
                }
            }
        }
        int count = 0;
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(game))
        {
            for (Path entry : entries)
            {
                if (!Files.isRegularFile(entry)
                    || !entry.getFileName().toString().endsWith(".java"))
                {
                    throw new AssertionError("Non-Java content in package: " + entry);
                }
                String content = new String(Files.readAllBytes(entry),
                    StandardCharsets.UTF_8);
                if (!content.trim().startsWith("package SlayVTGame;"))
                {
                    throw new AssertionError("Unexpected Java package: " + entry);
                }
                count++;
            }
        }
        if (count == 0)
        {
            throw new AssertionError("Java game source is empty.");
        }
        System.out.println("PASS: clean source layout; " + count + " Java files.");
    }
}
