package ru.vsu.cs.Lukashev.service;

import com.vdurmont.emoji.EmojiParser;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
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
import ru.vsu.cs.Lukashev.model.entity.Event;
import ru.vsu.cs.Lukashev.model.entity.Folder;
import ru.vsu.cs.Lukashev.model.entity.User;
import ru.vsu.cs.Lukashev.model.repository.EventRepository;
import ru.vsu.cs.Lukashev.model.repository.UserRepository;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.*;

@Slf4j
@Component
@Transactional
public class TelegramBot extends TelegramLongPollingBot {

    private static final String ADD_FOLDER_BUTTON                   = "ADD_FOLDER_BUTTON";
    private static final String ADD_EVENT_BUTTON                    = "ADD_EVENT_BUTTON";
    private static final String CHOOSE_MONTH_BUTTON                 = "\\d{1,2}";
    private static final String CHOOSE_DAY_OF_MONTH_BUTTON          = "\\d{1,2}.\\d{1,2}";
    private static final String SAME_MESSAGE_BUTTON                 = "GO_TO_MESSAGE_";
    private static final String YES_ENTER_EVENT_NAME_BUTTON         = "YES_ENTER_EVENT_NAME_BUTTON";
    private static final String ENTER_EVENT_NAME_BUTTON             = "ENTER_EVENT_NAME_BUTTON";
    private static final String NO_ENTER_EVENT_NAME_BUTTON          = "NO_ENTER_EVENT_NAME_BUTTON";
    private static final String YES_ENTER_EVENT_DESCRIPTION_BUTTON  = "YES_ENTER_EVENT_DESCRIPTION_BUTTON";
    private static final String NO_ENTER_EVENT_DESCRIPTION_BUTTON   = "NO_ENTER_EVENT_DESCRIPTION_BUTTON";

//    private User temporaryUser;
    private Folder temporaryFolder;
//    private Perm temporaryUser;
    private Event temporaryEvent;



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
        /**
         * command with "/"
         */
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/")){

            String messageText  = update.getMessage().getText();
            long chatId         = update.getMessage().getChatId();
            System.out.println("Command " + messageText);
            switch (messageText) {
                case "/start"       -> createStartCommand(update, chatId);
                case "/add"         -> createAddCommand(chatId);
                case "/my_calendar" -> createMyCalendarCommand(chatId);
                default             -> defaultAnswer(chatId);
            }
            setLastCommand(messageText);

        }
        /**
         * command without "/"
         */
        else if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText  = update.getMessage().getText();
            long chatId         = update.getMessage().getChatId();
            System.out.println("Command without */* " + getLastCommand());
//            System.out.println("Command without */* " + messageText);
            switch (getLastCommand()) {
                case ENTER_EVENT_NAME_BUTTON            -> generateDoYouWantEnterEventDescriptionButton(chatId, messageText);
                case YES_ENTER_EVENT_DESCRIPTION_BUTTON -> eventAdd(chatId, messageText);
                case ADD_FOLDER_BUTTON                  -> eventAdd(chatId, messageText);
                default             -> defaultAnswer(chatId);
            }
            setLastCommand(messageText);

            System.out.println(temporaryEvent);

        }
        /**
         * command with CallbackQuery  (update)
         */
        else if (update.hasCallbackQuery()){
            String callbackData = curMessageForEdit.getData();
            long messageId      = curMessageForEdit.getMessage().getMessageId();
            long chatId         = curMessageForEdit.getMessage().getChatId();
            System.out.println("update " + callbackData);
//            System.out.println(callbackData.matches(CHOOSE_DAY_OF_MONTH_BUTTON));

            if(callbackData.equals(ADD_FOLDER_BUTTON)){
                sendMessageEnterSomething(messageId, chatId, "название");


//                generateEventOrFolderButton(messageId, chatId, "Нету пока, отдохни, брат");
                setLastCommand(callbackData);

            } else if (callbackData.equals(ADD_EVENT_BUTTON)) {

                generateChooseMonthButton(messageId, chatId);

//                generateCalendar(chatId);
//                generateEventOrFolderMes(messageId, chatId, "И тут похуй");
                setLastCommand(callbackData);

            } else if (callbackData.matches(CHOOSE_MONTH_BUTTON)) {

                generateChooseDayOfMonthButton(messageId, chatId, callbackData);

                System.out.println("================================");
                setLastCommand(callbackData);

            } else if (callbackData.matches(CHOOSE_DAY_OF_MONTH_BUTTON)){

                sendMessageEnterSomething(messageId, chatId, "название");

//                generateDoYouWantEnterEventNameButton(messageId, chatId, callbackData);


//                enterEventName(messageId, chatId, callbackData);
//
//
                String[] parseDate = callbackData.split("\\.");
//
                Date dateTime = Date.valueOf(LocalDate.of(2023, parseInt(parseDate[1]), parseInt(parseDate[0])));
//
                this.temporaryEvent = new Event();
//
                temporaryEvent.setDate(dateTime);
                temporaryEvent.setOwnerID(userRepository.findById(chatId).isPresent() ?
                                            userRepository.findById(chatId).get() : null);

//                temporaryEvent.set
//                addEventMessage(messageId, chatId, callbackData);

                setLastCommand(ENTER_EVENT_NAME_BUTTON);
            }
//            else if (callbackData.startsWith(ENTER_EVENT_NAME_BUTTON)) {
//                sendMessageEnterSomething(messageId, chatId, "название");
//                setLastCommand(ENTER_EVENT_NAME_BUTTON);
//            }
            else if (callbackData.equals(YES_ENTER_EVENT_DESCRIPTION_BUTTON)) {
                sendMessageEnterSomething(messageId, chatId, "описание");
                setLastCommand(callbackData);
            }else if (callbackData.equals(NO_ENTER_EVENT_DESCRIPTION_BUTTON)) {
                eventAdd(chatId, "");
//                sendMessageEnterSomething(messageId, chatId, "описание");
                setLastCommand(callbackData);
            }
            // getLastCommand() так как обращаемся к предыдущей команде
            else if (callbackData.startsWith(SAME_MESSAGE_BUTTON)) {
                String[] parts = callbackData.split("_");

                generateChooseDayOfMonthButton(Long.parseLong(parts[4]), Long.parseLong(parts[3]), getLastCommand());


            }

        }


    }

    /**
     * START info message
     */

    private void eventAdd(long chatId, String eventDescription){
        temporaryEvent.setDescription(eventDescription);
        eventRepository.save(temporaryEvent);
//        temporaryEvent = new ;

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM");
        message.setText("Добавлено событие <b>" + temporaryEvent.getName() + "</b> на дату " +
                dateFormat.format(temporaryEvent.getDate()) + " . Обращайтесь! :)");
        message.enableHtml(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }
    }


    /**
     * END info message
     * -----------------------------------
     * START input text processing
     */



    private void sendMessageEnterSomething(long messageId, long chatId, String text){
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId((int) messageId);
        message.setDisableWebPagePreview(true);
        message.setText("Напишите, пожалуйста, " + text + ".");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sendMessageEnterEventName: " + e);
        }
    }


//    private void addEventName(String eventName){
//
//        this.temporaryEvent.setName(eventName);
//
//    }

    /**
     * END input text processing
     * ---------------------------------
     * START button processing area
     */

    private void generateDoYouWantEnterEventDescriptionButton(long chatId, String eventName){

        temporaryEvent.setName(eventName);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Хотите добавить описание событию?");

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData(YES_ENTER_EVENT_DESCRIPTION_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData(NO_ENTER_EVENT_DESCRIPTION_BUTTON);



        rowInline.add(yesButton);
        rowInline.add(noButton);
//        rowInline.add(addFolderButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }

    }
    private void generateDoYouWantEnterEventNameButton(long messageId, long chatId, String callbackData) {

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setDisableWebPagePreview(true);
        message.setText("Задать название?");
        message.setMessageId((int) messageId);


//        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//        List<InlineKeyboardButton> rowInline        = new ArrayList<>();
//
//        var yesButton = new InlineKeyboardButton();
//        yesButton.setText("Да");
////        yesButton.setSwitchInlineQueryCurrentChat("/event_name: ");
//        yesButton.setCallbackData(YES_ENTER_EVENT_NAME_BUTTON);
//
//        var noButton = new InlineKeyboardButton();
//        noButton.setText("Нет");
////        yesButton.setSwitchInlineQueryCurrentChat("/event_name: ");
//        yesButton.setCallbackData(NO_ENTER_EVENT_NAME_BUTTON);
////        setLastCommand(YES_ENTER_EVENT_NAME_BUTTON);
//        rowInline.add(yesButton);
//        rowInline.add(noButton);
//
//
//
//        rowsInline.add(rowInline);
//        markupInline.setKeyboard(rowsInline);
//        message.setReplyMarkup(markupInline);



//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(chatId);
//        sendMessage.setText("Введите сообщение:");
//        sendMessage.setReplyMarkup(new ForceReply());
//        sendMessage.setReplyMarkup(replyMarkup);
//        execute(sendMessage);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }






    }
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




        Month curMonth = Month.of(parseInt(month));

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

    private void createMyCalendarCommand(long chatId) {


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
