package SlayVTGame;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Checks the repository's required compile and run instructions. */
public class ReadmeTest
{
    public static void main(String[] args) throws IOException
    {
        Path readme = Paths.get("README.md");
        if (!Files.isRegularFile(readme))
        {
            readme = Paths.get("..").resolve("README.md");
        }
        if (!Files.isRegularFile(readme))
        {
            throw new AssertionError("Missing repository README.md.");
        }

        String text = new String(Files.readAllBytes(readme),
            StandardCharsets.UTF_8);
        require(text, "# SlayVT");
        require(text, "## Requirements");
        require(text, "## Compile and run");
        require(text, "javac -d build SlayVT/src/SlayVTGame/*.java");
        require(text, "java -cp build SlayVTGame.Main");
        require(text, "### Eclipse");
        require(text, "## Tests");
        System.out.println("PASS: README compile and run instructions.");
    }

    private static void require(String text, String expected)
    {
        if (!text.contains(expected))
        {
            throw new AssertionError("README is missing: " + expected);
        }
    }
}
