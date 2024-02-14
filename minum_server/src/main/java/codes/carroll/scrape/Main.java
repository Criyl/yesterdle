package codes.carroll.scrape;

import com.renomad.minum.web.FullSystem;
import com.renomad.minum.database.Db;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import codes.carroll.WordModel;

import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.List;
import java.lang.Thread;
import java.time.Duration;
import java.lang.InterruptedException;

public class Main {

    private static void start(WebDriver driver) throws InterruptedException {
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(d -> driver.findElement(By.tagName("body")).isEnabled());

        WebElement playButton = driver.findElement(By.xpath("//button[@data-testid='Play']"));
        playButton.click();

        wait.until(d -> driver.findElement(By.xpath("//button[@aria-label='Close']")).isEnabled());
        WebElement closeButton = driver.findElement(By.xpath("//button[@aria-label='Close']"));
        closeButton.sendKeys(Keys.RETURN);
    }

    private static void enterGuess(WebDriver driver, String guess) throws InterruptedException {

        WebElement body = driver.findElement(By.tagName("body"));
        body.sendKeys(guess.toString() + Keys.RETURN);
        Thread.sleep(3000);
    }

    private static void add(String word) {
        FullSystem fs = FullSystem.initialize();
        Db<WordModel> words = fs.getContext().getDb("words", WordModel.EMPTY);

        for (WordModel model : words.values()) {
            if (model.word.matches(word)) {
                return;
            }
        }

        WordModel wordModel = new WordModel(0, word);
        words.write(wordModel);
    }

    private static String solve(WebDriver driver) throws InterruptedException {
        start(driver);
        Thread.sleep(3000);
        enterGuess(driver, "scram");
        enterGuess(driver, "scram");
        enterGuess(driver, "scram");
        enterGuess(driver, "scram");
        enterGuess(driver, "scram");
        enterGuess(driver, "scram");

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(d -> driver.findElement(By.xpath("//div[@aria-live='polite']")).isEnabled());
        String results = driver.findElement(By.xpath("//div[@aria-live='polite']")).getText();
        return results;
    }

    public static void main(String[] args) throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(List.of(
                "--ignore-ssl-errors=yes",
                "--ignore-certificate-errors"));
        String seleniumUri = System.getenv("SELENIUM_URI");
        WebDriver driver = new RemoteWebDriver(new URL(seleniumUri), options);

        driver.get("https://www.nytimes.com/games/wordle");

        String result = solve(driver);
        add(result);
        System.out.println(result);
        driver.quit();
    }

}
