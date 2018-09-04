import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import redis.clients.jedis.Jedis;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Jedis redis = new Jedis();
        var token = "";
        var userName = "";
        Scanner scanner = new Scanner(System.in);
        if (redis.get("PMResanTokEn")==null){
            System.out.println("Please enter your bot token : ");
            token = scanner.nextLine();
            redis.set("PMResanTokEn",token);
        }
        if (redis.get("PMResanUserName")==null){
            System.out.println("Please enter your bot username without @ : ");
            userName = scanner.nextLine();
            redis.set("PMResanUserName",userName);
        }
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try{
            telegramBotsApi.registerBot(new PMResan());
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
        System.out.println("Bots Started!");
    }
}
