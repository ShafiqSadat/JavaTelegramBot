import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class PMResan extends TelegramLongPollingBot {
    Jedis redis = new Jedis();
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            var messageText = update.getMessage().getText();
            var fromID = update.getMessage().getFrom().getId();
            if (messageText.equals("/start")&&isAdmin(fromID)){
                if (!redis.sismember("PMResanMembers",String.valueOf(fromID))){
                    redis.sadd("PMResanMembers",String.valueOf(fromID));
                }
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                List<InlineKeyboardButton> row2 = new ArrayList<>();
                List<InlineKeyboardButton> row3 = new ArrayList<>();
                List<InlineKeyboardButton> row4 = new ArrayList<>();
                List<InlineKeyboardButton> row6 = new ArrayList<>();
                List<InlineKeyboardButton> row5 = new ArrayList<>();
                row1.add(new InlineKeyboardButton().setText("تنظیم پروفایل \uD83D\uDCEB").setCallbackData("setProfile"));
                row2.add(new InlineKeyboardButton().setText("تنظیم متن استارت \uD83D\uDCF0").setCallbackData("setWelcome"));
                row2.add(new InlineKeyboardButton().setText("تنظیم کانال \uD83D\uDCE3").setCallbackData("setChannel"));
                row3.add(new InlineKeyboardButton().setText("افزودن ادمین \uD83D\uDC68\u200D\uD83D\uDCBB").setCallbackData("setAdmin"));
                row4.add(new InlineKeyboardButton().setText("آمار \uD83D\uDCC8").setCallbackData("status"));
                row5.add(new InlineKeyboardButton().setText("بستن کیبورد \uD83D\uDCA2").setCallbackData("exitPanel"));
                rows.add(row1);
                rows.add(row2);
                rows.add(row3);
                rows.add(row4);
                rows.add(row6);
                rows.add(row5);
                inlineKeyboardMarkup.setKeyboard(rows);
                SendMessage sendMessage = new SendMessage()
                        .setChatId(String.valueOf(fromID))
                        .setReplyMarkup(inlineKeyboardMarkup)
                        .setText("ادمین عزیز خوش آمدید به ربات خود \nبرای ادامه از دکمه های زیر استفاده کنید !");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (update.hasMessage() && update.getMessage().hasText()&&!isAdmin(fromID)){
                if (messageText.equals("/start")){
                    if (!redis.sismember("PMResanMembers",String.valueOf(fromID))){
                        redis.sadd("PMResanMembers",String.valueOf(fromID));
                    }
                    var channel = "";
                    if (redis.get("TeamChannelLink")!=null){
                        channel = redis.get("TeamChannelLink");
                    }
                    else{
                        channel = "https://t.me/afbots";
                    }
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    List<InlineKeyboardButton> row3 = new ArrayList<>();
                    row1.add(new InlineKeyboardButton().setText("درخواست پشتیبانی \uD83D\uDC68\u200D\uD83D\uDE80").setCallbackData("support"));
                    row2.add(new InlineKeyboardButton().setText("معلومات در مورد تیم \uD83D\uDC65").setCallbackData("about"));
                    row3.add(new InlineKeyboardButton().setText("کانال تیم \uD83D\uDCE3").setUrl(channel));
                    rows.add(row1);
                    rows.add(row2);
                    rows.add(row3);
                    inlineKeyboardMarkup.setKeyboard(rows);
                    var text = "";
                    var toText = "";
                    if (redis.get("WelcomeText")!=null){
                        text = redis.get("WelcomeText");
                        toText = text.replaceAll("NAME",update.getMessage().getFrom().getFirstName());
                    }
                    else{
                        toText = update.getMessage().getFrom().getFirstName() +" خوش آمدید \nبرای ادامه از دکمه های زیر انتخاب کنید !";
                    }
                    SendMessage sendMessage = new SendMessage()
                            .setText(toText)
                            .setChatId(update.getMessage().getChatId())
                            .setReplyMarkup(inlineKeyboardMarkup);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    if (CheckUser(update, fromID)) return;
                    SendMessage sendMessage = new SendMessage()
                            .setReplyToMessageId(update.getMessage().getMessageId())
                            .setChatId(String.valueOf(fromID))
                            .setText("♻️ پاسخ سیستم \uD83D\uDC47\n" +
                                    "\n" +
                                    "پیام شما با موفقیت به پشتیبانی ارسال شد منتظر جواب باشید و از ارسال پیام تکراری خود داری کنید !");
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    } finally {
                        redis.set("PmLastMessageID" + fromID, String.valueOf(update.getMessage().getMessageId()));
                    }
                }
            }
            else if (update.getMessage().hasText() && isAdmin(update.getMessage().getChatId())&&redis.get("waitForProfileText")!=null){
                redis.set("TeamProfileText",messageText);
                redis.del("waitForProfileText");
                sendMessageText("متن ارسال شده با موفقیت ذخیره شد !",update.getMessage().getChatId());
            }
            else if (update.getMessage().hasText() && isAdmin(update.getMessage().getChatId())&&redis.get("waitForChannelLink")!=null){
                redis.set("TeamChannelLink",messageText);
                redis.del("waitForChannelLink");
                sendMessageText("متن ارسال شده با موفقیت ذخیره شد !",update.getMessage().getChatId());
            }
            else if (update.getMessage().hasText() && isAdmin(update.getMessage().getChatId()) && redis.get("waitForAdminID")!=null){
                redis.sadd("PmAdmins",messageText);
                redis.del("waitForAdminID");
                sendMessageText("ایدی ارسال شده ذخیره شد !",update.getMessage().getChatId());
            }
            else if (update.getMessage().hasText() && isAdmin(update.getMessage().getChatId()) && redis.get("waitForWelcomeTextText")!=null){
                redis.del("waitForWelcomeTextText");
                redis.set("WelcomeText", messageText);
                sendMessageText("متن ارسال شده با موفقیت ذخیره شد !",update.getMessage().getChatId());
            }
            ReplyMessage(update, fromID);
        }
        else if (update.hasMessage() && update.getMessage().hasPhoto()|update.getMessage().hasAnimation()|update.getMessage().hasContact()|update.getMessage().hasDocument()|update.getMessage().hasLocation()|update.getMessage().hasVideo()|update.getMessage().hasVideoNote()&&!isAdmin(update.getMessage().getChatId())){
            var fromID = update.getMessage().getFrom().getId();
            ReplyMessage(update, fromID);
            if (CheckUser(update, fromID)) return;
            SendMessage sendMessage = new SendMessage()
                    .setReplyToMessageId(update.getMessage().getMessageId())
                    .setChatId(String.valueOf(fromID))
                    .setText("♻️ پاسخ سیستم \uD83D\uDC47\n" +
                            "\n" +
                            "پیام شما با موفقیت به پشتیبانی ارسال شد منتظر جواب باشید و از ارسال پیام تکراری خود داری کنید !");
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            } finally {
                redis.set("PmLastMessageID" + fromID, String.valueOf(update.getMessage().getMessageId()));
            }
        }
        else if (update.hasMessage() && update.getMessage().hasSticker()&&!isAdmin(update.getMessage().getChatId())){
            var fromID = update.getMessage().getFrom().getId();
            ReplyMessage(update, fromID);
            if (CheckUser(update, fromID)){
                return;
            }
            SendMessage sendMessage = new SendMessage()
                    .setReplyToMessageId(update.getMessage().getMessageId())
                    .setChatId(String.valueOf(fromID))
                    .setText("♻️ پاسخ سیستم \uD83D\uDC47\n" +
                            "\n" +
                            "پیام شما با موفقیت به پشتیبانی ارسال شد منتظر جواب باشید و از ارسال پیام تکراری خود داری کنید !");
            try {
                execute(sendMessage);
                for (String admin : redis.smembers("PmAdmins")) {
                    sendMessageText("استیکر ارسال شده از طرف "+"["+update.getMessage().getFrom().getFirstName()+"](tg://user?id="+update.getMessage().getFrom().getId()+")",Long.parseLong(admin));
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            } finally {
                redis.set("PmLastMessageID" + fromID, String.valueOf(update.getMessage().getMessageId()));
            }
        }
        else if(update.hasCallbackQuery()){
            var callBack = update.getCallbackQuery().getData();
            var fromID = update.getCallbackQuery().getFrom().getId();
            var chatID = update.getCallbackQuery().getMessage().getChatId();
            if (callBack.equals("support")){
                EditMessageText editMessageText = new EditMessageText()
                        .setText("درخواست پشتیبانی شما ارسال متنظر تائید از طرف تیم باشید !")
                        .setChatId(chatID)
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                List<InlineKeyboardButton> row2 = new ArrayList<>();
                row1.add(new InlineKeyboardButton().setText("تائید ✅").setCallbackData("accept"+fromID));
                row2.add(new InlineKeyboardButton().setText("رد ❌").setCallbackData("reject"+fromID));
                rows.add(row1);
                rows.add(row2);
                inlineKeyboardMarkup.setKeyboard(rows);
                SendMessage sendMessage = new SendMessage()
                        .setText("درخواست پشتیبانی از طرف "+"["+update.getCallbackQuery().getFrom().getFirstName()+"](tg://user?id="+update.getCallbackQuery().getFrom().getId()+") "+"برای تائید یا رد درخواست از دکمه های زیر استفاده کنید !")
                        .enableMarkdown(true)
                        .setReplyMarkup(inlineKeyboardMarkup);
                try {
                    for (String admins : redis.smembers("PmAdmins")) {
                        sendMessage.setChatId(admins);
                        execute(sendMessage);
                    }
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (callBack.equals("exitPanel")){
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery()
                        .setCallbackQueryId(update.getCallbackQuery().getId())
                        .setText("باشد \uD83D\uDE10")
                        .setShowAlert(true);
                DeleteMessage deleteMessage = new DeleteMessage()
                        .setChatId(chatID)
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                try {
                    execute(answerCallbackQuery);
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (callBack.equals("about")){
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                row1.add(new InlineKeyboardButton().setText("بازگشت ↩").setCallbackData("backFirst"));
                rows.add(row1);
                inlineKeyboardMarkup.setKeyboard(rows);
                var text = "ثبت نشده";
                if (redis.get("TeamProfileText")!=null){
                    text = redis.get("TeamProfileText");
                }
                EditMessageText editMessageText = new EditMessageText()
                        .setText(text)
                        .setChatId(chatID)
                        .setReplyMarkup(inlineKeyboardMarkup)
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (callBack.equals("setChannel")){
                sendMessageText("حال لینک کانال را ارسال کنید !\nتوجه داشتید باشید که لینک حتما باید به این فرمت باشد \nhttp://t.me/YOUR_LINK",chatID);
                redis.set("waitForChannelLink","Yet");
            }
            else if (callBack.equals("setAdmin")){
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery()
                        .setText("حال ایدی عددی شخص مورد نظر را ارسال کنید !")
                        .setShowAlert(true)
                        .setCallbackQueryId(update.getCallbackQuery().getId());
                try {
                    redis.set("waitForAdminID","Yet");
                    execute(answerCallbackQuery);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (callBack.equals("setProfil")){
                sendMessageText("حال متن برای پروفایل را ارسال کنید \nتوجه کنید که متن پروفایل هرچی بیشتر ایموجی و متن داشته باشد بهتر است !",chatID);
                redis.set("waitForProfileText","Yet");
            }
            else if(callBack.equals("setWelcome")){
                sendMessageText("حال متن استارت را ارسال کنید \nشما میتوانید اسم شخص راهم استفاده کنید مثلا » سلام NAME خوش آمدی \nدرینجا بجای NAME اسم شخص نوشته خواهد شد ",chatID);
                redis.set("waitForWelcomeTextText","Yet");
            }
            else if (callBack.equals("backFirst")){
                var channel = "";
                if (redis.get("TeamChannelLink")!=null){
                    channel = redis.get("TeamChannelLink");
                }
                else{
                    channel = "https://t.me/afbots";
                }
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                List<InlineKeyboardButton> row2 = new ArrayList<>();
                List<InlineKeyboardButton> row3 = new ArrayList<>();
                row1.add(new InlineKeyboardButton().setText("درخواست پشتیبانی \uD83D\uDC68\u200D\uD83D\uDE80").setCallbackData("support"));
                row2.add(new InlineKeyboardButton().setText("معلومات در مورد تیم \uD83D\uDC65").setCallbackData("about"));
                row3.add(new InlineKeyboardButton().setText("کانال تیم \uD83D\uDCE3").setUrl(channel));
                rows.add(row1);
                rows.add(row2);
                rows.add(row3);
                inlineKeyboardMarkup.setKeyboard(rows);
                var toText = "خب برگشتیم چه کاری برات انجام بدم ؟";
                EditMessageText sendMessage = new EditMessageText()
                        .setText(toText)
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setChatId(chatID)
                        .setReplyMarkup(inlineKeyboardMarkup);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (callBack.equals("status")){
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery()
                        .setShowAlert(true)
                        .setText("آمار ربات شما : "+redis.scard("PMResanMembers")+" ممبر است !")
                        .setCallbackQueryId(update.getCallbackQuery().getId());
                try {
                    execute(answerCallbackQuery);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (callBack.contains("accept")){
                var userid = callBack.replaceAll("accept","");
                sendMessageText("درخواست پشتبانی شما قبول شد ✅\nحال میتوانید پیام خود را ارسال کنید !",Integer.parseInt(userid));
                redis.set("userHaveSupport"+userid,"Yes");
                EditMessageText editMessageText = new EditMessageText()
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setChatId(chatID)
                        .setText("درخواست پشتیبانی شخص مورد نظر تائید شد !\n");
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            else if (callBack.contains("reject")){
                var userid = callBack.replaceAll("reject","");
                sendMessageText("درخواست پشتیبانی شما رد شد ❌\nلطفا بعدا درخواست بدهید و از ارسال درخواست های مکرر خودداری کنید !",Integer.parseInt(userid));
                redis.del("userHaveSupport"+userid);
                EditMessageText editMessageText = new EditMessageText()
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setChatId(chatID)
                        .setText("درخواست پشتیبانی شخص مورد نظر تائید شد !\n");
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean CheckUser(Update update, Integer fromID) {
        if (redis.get("userHaveSupport"+fromID)!=null){
            for (String admins : redis.smembers("PmAdmins")) {
                forwardMessage(fromID, Long.parseLong(admins), update.getMessage().getMessageId());
            }
        }
        else {
            sendMessageText("شما درخواستی برای پشتیبانی ارسال نکردید !\nبرای درخواست پشتیبانی ربات را مجدد ارسال کنید و سپس روی دکمه درخواست پشتیبانی کلیک کنید \nبرای ادامه روی استارت کلیک کنید \n/start",update.getMessage().getChatId());
            return true;
        }
        return false;
    }

    private void ReplyMessage(Update update, Integer fromID) {
        if (update.getMessage().getReplyToMessage()!=null&&isAdmin(update.getMessage().getChatId())){
            var touser = update.getMessage().getReplyToMessage().getForwardFrom().getId();
            SendMessage sendMessage = new SendMessage()
                    .setChatId(String.valueOf(touser))
                    .setReplyToMessageId(Integer.valueOf(redis.get("PmLastMessageID"+touser)))
                    .setText("♻️ پاسخ پشتیبانی \uD83D\uDC47 \n"+update.getMessage().getText());
            try {
                execute(sendMessage);
                sendMessage.setChatId(String.valueOf(fromID));
                sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
                sendMessage.setText("پیام شما با موفقیت به کاربر ارسال شد!");
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return redis.get("PMResanUserName");
    }
    private void sendMessageText (String text,long chatID){
        SendMessage sendMessage =  new SendMessage()
                .setChatId(chatID)
                .enableMarkdown(true)
                .setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
    private void forwardMessage(long from,long to,int messageid){
        ForwardMessage forwardMessage = new ForwardMessage()
                .setMessageId(messageid)
                .setFromChatId(from)
                .setChatId(to);
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private boolean isAdmin(long chatid){
        return redis.sismember("PmAdmins", String.valueOf(chatid));
    }
    @Override
    public String getBotToken() {
        return redis.get("PMResanTokEn");
    }
}
