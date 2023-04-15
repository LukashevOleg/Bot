package ru.vsu.cs.Lukashev.service;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.vsu.cs.Lukashev.calendarData.DayOfWeekEnum;
import ru.vsu.cs.Lukashev.calendarData.MonthEnum;
import ru.vsu.cs.Lukashev.config.BotConfig;
import ru.vsu.cs.Lukashev.model.entity.User;
import ru.vsu.cs.Lukashev.model.repository.EventRepository;
import ru.vsu.cs.Lukashev.model.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Transactional
public class TelegramBot extends TelegramLongPollingBot {

    private static final String ADD_FOLDER_BUTTON               = "ADD_FOLDER_BUTTON";
    private static final String ADD_EVENT_BUTTON                = "ADD_EVENT_BUTTON";
    private static final String CHOOSE_MONTH_BUTTON             = "\\d{1,2}";
    private static final String CHOOSE_DAY_OF_MONTH_BUTTON      = "\\d{1,2}.\\d{1,2}";
    private static final String SAME_MESSAGE_BUTTON             = "GO_TO_MESSAGE_";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;

    private final BotConfig botConfig;
    private String lastCommand = "";

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/add",          "Add your new Event or Folder"));
        commandList.add(new BotCommand("/my_calendar",  "Let's see your near Events!"));
        commandList.add(new BotCommand("/permissions",  "Let's see your near Events!"));
        commandList.add(new BotCommand("/settings",     "You can change your Events!"));
        try {
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var curMessageForEdit = update.getCallbackQuery();
        if (update.hasMessage() && update.getMessage().hasText()){

            String messageText  = update.getMessage().getText();
            long chatId         = update.getMessage().getChatId();

            switch (messageText) {
                case "/start"       -> createStartCommand(update, chatId);
                case "/add"         -> createAddCommand(chatId);
                case "/my_calendar" -> createMyCalendarCommand(chatId);
                default             -> defaultAnswer(chatId);
            }
            setLastCommand(messageText);
        }
        else if (update.hasCallbackQuery()){
            String callbackData = curMessageForEdit.getData();
            long messageId      = curMessageForEdit.getMessage().getMessageId();
            long chatId         = curMessageForEdit.getMessage().getChatId();
            System.out.println(callbackData);
            System.out.println(callbackData.matches(CHOOSE_DAY_OF_MONTH_BUTTON));

            if(callbackData.equals(ADD_FOLDER_BUTTON)){



                generateEventOrFolderButton(messageId, chatId, "Нету пока, отдохни, брат");
                setLastCommand(callbackData);

            } else if (callbackData.equals(ADD_EVENT_BUTTON)) {

                generateChooseMonthButton(messageId, chatId);

//                generateCalendar(chatId);
//                generateEventOrFolderMes(messageId, chatId, "И тут похуй");
                setLastCommand(callbackData);

            } else if (callbackData.matches(CHOOSE_MONTH_BUTTON)) {

                generateChooseDayOfMonthButton(messageId, chatId, callbackData);

//                generateEventOrFolderButton(messageId, chatId, "пошел нахуй аххаха выбрал он хахаха ты завтра хотя бы блять " +
//                                                                        "спланируй еблан. Планировать собрался");
                System.out.println("================================");
                setLastCommand(callbackData);

            } else if (callbackData.matches(CHOOSE_DAY_OF_MONTH_BUTTON)){
                addEventMessage(messageId, chatId, callbackData);

                setLastCommand(callbackData);
            }
            // getLastCommand() так как обращаемся к предидущей команде
            else if (callbackData.startsWith(SAME_MESSAGE_BUTTON)) {
                String[] parts = callbackData.split("_");

                generateChooseDayOfMonthButton(Long.parseLong(parts[4]), Long.parseLong(parts[3]), getLastCommand());


            }

        }


    }

    private void createMyCalendarCommand(long chatId) {


    }

    private void addEventMessage(long messageId, long chatId, String date){
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setDisableWebPagePreview(true);
        message.setText("записал " + date);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("addEventMessage method error: " + e);
        }
    }

    /**
     * START button processing area
     */

    private void generateChooseDayOfMonthButton(long messageId, long chatId, String month){
        System.out.println("I am in generateChooseDayOfMonthButton");

        DayOfWeekEnum[] dayOfWeekArr = DayOfWeekEnum.values();


//        List<MonthEnum> monthList = Arrays.asList(MonthEnum.values());
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setDisableWebPagePreview(true);
        message.setText("Выберите день");
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup keyboardMarkup         = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard   = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        for(DayOfWeekEnum curDay : dayOfWeekArr){
            var monthButton = new InlineKeyboardButton(curDay.getName());
            monthButton.setCallbackData(String.valueOf(curDay.getNumber()));
            rowInline.add(monthButton);
        }
        keyboard.add(rowInline);
        rowInline = new ArrayList<>();




        Month curMonth = Month.of(Integer.parseInt(month));

        int firstDayOfMonth = LocalDate.parse(month.matches("\\d") ? ("01.0" + month + ".2023")
                                              : ("01." + month + ".2023"),
                                              DateTimeFormatter.ofPattern("dd.MM.yyyy")).getDayOfWeek().getValue();

        String textForEmptyButton = EmojiParser.parseToUnicode(":wavy_dash:");
        InlineKeyboardButton emptyButton = new InlineKeyboardButton();
        emptyButton.setText(textForEmptyButton);
        emptyButton.setCallbackData(SAME_MESSAGE_BUTTON + chatId + "_" + messageId);
//        emptyButton.setSwitchInlineQuery(" ");

        // заполняем пустоты, подгоняем 1 число под день недели
        for(int i = 0; i < firstDayOfMonth - 1; i++){
            rowInline.add(emptyButton);
        }

        // заполняем весь месяц даты
        int i = 1;
        while (i < curMonth.length(false) + 1){
            if(rowInline.size() != 7){
                var dayOfMonthButton = new InlineKeyboardButton(String.valueOf(i));
                dayOfMonthButton.setCallbackData(i + "." + month);
                rowInline.add(dayOfMonthButton);
                i++;
            } else {
                keyboard.add(rowInline);
                rowInline = new ArrayList<>();
            }

        }



//        for(int i = 1; i < curMonth.length(false); i++){
//
//
//
//            if((firstDayOfMonth + i - 1) % 8 == 0){
//                keyboard.add(rowInline);
//                rowInline = new ArrayList<>();
//            }
//
//            var dayOfMonthButton = new InlineKeyboardButton(String.valueOf(i));
//            dayOfMonthButton.setCallbackData(String.valueOf(i));
//            rowInline.add(dayOfMonthButton);
//        }

        // добивем пустыми кнопками до конца недели
        while (rowInline.size() != 7){
            rowInline.add(emptyButton);
        }
        keyboard.add(rowInline);





        // TODO: добавить кнопку "назад"

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("DayOfWeek method error: " + e);
        }

    }
    private void generateChooseMonthButton(long messageId, long chatId){
        System.out.println("I am in generateChooseMonthButton");
        MonthEnum[] monthList = MonthEnum.values();
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText("Выберите месяц");
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup keyboardMarkup         = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard   = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();


        // TODO: добавить кнопку "назад"
        int i = 0;
        for(MonthEnum curMonth : monthList){

            if(i % 3 == 0) {
                keyboard.add(rowInline);
                rowInline = new ArrayList<>();
            }

            var monthButton = new InlineKeyboardButton(curMonth.getName());
            monthButton.setCallbackData(String.valueOf(curMonth.getNumber()));
            rowInline.add(monthButton);
            i++;
        }
        keyboard.add(rowInline);


        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }
    private void generateEventOrFolderButton(long messageId, long chatId, String textMes){
        EditMessageText mes = new EditMessageText();
        mes.setChatId(chatId);
        mes.setText(textMes);
        mes.setMessageId((int) messageId);
        try {
            execute(mes);
        } catch (TelegramApiException e) {

        }
    }

    /**
     * END button processing area
     * -----------------------------------
     *  START command processing area
     */

    private void createStartCommand(Update update, long chatId){

        registerUser(update.getMessage());
        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
    }



    private void createAddCommand(long chatId){
//        message(chatId, "Хотите создать событие или папку?");
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Хотите создать событие или папку?");

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();
        var addFolderButton                         = new InlineKeyboardButton();

        addFolderButton.setText("Папку");
        addFolderButton.setCallbackData("ADD_FOLDER_BUTTON");

        var addEventButton = new InlineKeyboardButton();

        addEventButton.setText("Событие");
        addEventButton.setCallbackData("ADD_EVENT_BUTTON");

        rowInline.add(addEventButton);
        rowInline.add(addFolderButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }


    }


    private void defaultAnswer(long chatId){
        sendMessage(chatId, "Мимо, мимо, брат..");
    }
    private void registerUser(Message message) {

        if(userRepository.findById(message.getChatId()).isEmpty()){
            var chatId  = message.getChatId();
            var chat    = message.getChat();
            User user   = new User();

            user.setId(chatId);
            user.setName(chat.getFirstName());
            System.out.println(user);
            userRepository.save(user);
//            log.info();
        }

    }

    private void startCommandReceived(long chatId, String name){
        String answer = "Hi, " + name + "!";

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }



    private void generateButton(long chatId){




        SendMessage message = new SendMessage();
        message.setChatId(chatId); // Указываем идентификатор чата, в который отправляем сообщение
        message.setText("Выберите дату:");

        // Создаем клавиатуру
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();


    }

    private void generateCalendar1(long chatId){
        // Инициализируем объект бота

// Получаем текущую дату
        LocalDate currentDate = LocalDate.now();

// Получаем месяц текущей даты
        Month currentMonth = currentDate.getMonth();

// Создаем объект сообщения
        SendMessage message = new SendMessage();
        message.setChatId(chatId); // Указываем идентификатор чата, в который отправляем сообщение
        message.setText("Выберите дату:");

// Создаем клавиатуру
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

// Добавляем кнопки с датами для каждого дня текущего месяца
        int daysInMonth = currentMonth.length(currentDate.isLeapYear());
        int currentDay = 1;
        for (int i = 0; i < 6; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                if (i == 0 && j < currentDate.getDayOfWeek().getValue() % 7) {
//                    row.add(new InlineKeyboardButton(""));
                } else if (currentDay > daysInMonth) {
//                    row.add(new InlineKeyboardButton(""));
                } else {
                    System.out.println("ffffffffffffffffffffffffffffff");
                    LocalDate date = LocalDate.of(currentDate.getYear(), currentMonth, currentDay);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    InlineKeyboardButton keyboardButton = new InlineKeyboardButton(formatter.format(date));
//                    keyboardButton.setText(formatter.format(date));
                    keyboardButton.setCallbackData(formatter.format(date));
                    row.add(keyboardButton);
                    currentDay++;
                }
            }
            keyboard.add(row);
        }
        System.out.println(keyboard);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }
// Отправляем сообщение с клавиатурой
// executeSendMessage(message);
    }
}
