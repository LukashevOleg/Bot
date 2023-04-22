package ru.vsu.cs.Lukashev.service;

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
import ru.vsu.cs.Lukashev.entity.Event;
import ru.vsu.cs.Lukashev.entity.Folder;
import ru.vsu.cs.Lukashev.entity.User;
import ru.vsu.cs.Lukashev.repository.EventRepository;
import ru.vsu.cs.Lukashev.repository.FolderRepository;
import ru.vsu.cs.Lukashev.repository.UserRepository;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Integer.*;

@Slf4j
@Component
@Transactional
public class TelegramBot extends TelegramLongPollingBot {
    //COMMANDS
    private static final String START_COMMAND           = "/start";
    private static final String ADD_COMMAND             = "/add";
    private static final String MY_CALENDAR_COMMAND     = "/my_calendar";
    private static final String MY_FOLDERS_COMMAND      = "/my_folders";
    private static final String PERMISSIONS_COMMAND     = "/permissions";
    private static final String SETTINGS_COMMAND        = "/settings";


    //BUTTONS
    private static final String ADD_FOLDER_BUTTON                       = "ADD_FOLDER_BUTTON";
    private static final String ADD_EVENT_BUTTON                        = "ADD_EVENT_BUTTON";
    private static final String CHOOSE_MONTH_BUTTON                     = "\\d{1,2}";
    private static final String CHOOSE_DAY_OF_MONTH_BUTTON              = "\\d{1,2}.\\d{1,2}";
    private static final String SAME_MESSAGE_BUTTON                     = "GO_TO_MESSAGE_";
    private static final String YES_ENTER_EVENT_NAME_BUTTON             = "YES_ENTER_EVENT_NAME_BUTTON";
    private static final String ENTER_EVENT_NAME_BUTTON                 = "ENTER_EVENT_NAME_BUTTON";
    private static final String NO_ENTER_EVENT_NAME_BUTTON              = "NO_ENTER_EVENT_NAME_BUTTON";
    private static final String YES_ENTER_EVENT_DESCRIPTION_BUTTON      = "YES_ENTER_EVENT_DESCRIPTION_BUTTON";
    private static final String NO_ENTER_EVENT_DESCRIPTION_BUTTON       = "NO_ENTER_EVENT_DESCRIPTION_BUTTON";
    private static final String YES_ADD_EVENT_TO_FOLDER_BUTTON          = "YES_ADD_EVENT_TO_FOLDER_BUTTON";
    private static final String NO_ADD_EVENT_TO_FOLDER_BUTTON           = "NO_ADD_EVENT_TO_FOLDER_BUTTON";
    private static final String CHOOSE_EVENT_FOR_FOLDER_BUTTON          = "CHOOSE_EVENT_FOR_FOLDER_BUTTON_";
    private static final String SWITCH_PAGE_BUTTON                      = "SWITCH_PAGE_BUTTON:";
    private static final String ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON = "ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON";
    private static final String CHOOSE_FOLDER_BUTTON                    = "CHOOSE_FOLDER_BUTTON_";
    private static final String DELETE_EVENT_FROM_FOLDER_BUTTON         = "DELETE_EVENT_FROM_FOLDER_BUTTON_";
    private static final String INFO_ABOUT_EVENT_BUTTON                 = "INFO_ABOUT_EVENT_BUTTON_";
    private static final String DELETE_FOLDER_BUTTON                    = "DELETE_FOLDER_BUTTON_";
    private static final String SAVE_CHANGES_IN_FOLDER_BUTTON           = "SAVE_CHANGES_IN_FOLDER_BUTTON";


    //POSITIONS
    private static final String IN_CHOOSE_EVENT_FOR_FOLDER  = "IN_CHOOSE_EVENT_FOR_FOLDER:";
    private static final String IN_CHOOSE_FOLDER            = "IN_CHOOSE_FOLDER:";
    private static final String IN_CHOOSE_EVENT             = "IN_CHOOSE_EVENT:";




//    private User temporaryUser;
    private Folder temporaryFolder;
//    private Perm temporaryUser;
    private Event temporaryEvent;

    private List<Integer> temporaryEventIndexList = new ArrayList<>();

    private int currentPageNumber = 0;



    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private FolderRepository folderRepository;

    private final BotConfig botConfig;
//    private String lastCommand = "";
    private final Map<Long, String> lastCommandForEachUserMap = new HashMap<>();

    private void setLastCommand(Long chatId, String lastCommand){
        lastCommandForEachUserMap.put(chatId, lastCommand);
    }

    private String getLastCommand(Long chatId){
        return lastCommandForEachUserMap.get(chatId);
    }

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand(ADD_COMMAND,          "Добавь папку или событие"));
        commandList.add(new BotCommand(MY_CALENDAR_COMMAND,  "Посмотри свои ближайшие события"));
        commandList.add(new BotCommand(MY_FOLDERS_COMMAND,   "Посмотри свои папки"));
        commandList.add(new BotCommand(PERMISSIONS_COMMAND,  "Посмотри свои подписки/наблюдателей"));
        commandList.add(new BotCommand(SETTINGS_COMMAND,     "Настройки"));
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

//    public String getLastCommand() {
//        return lastCommand;
//    }

//    public void setLastCommand(String lastCommand) {
//        this.lastCommand = lastCommand;
//    }

    public List<Integer> getTemporaryEventIndexList() {
        return temporaryEventIndexList;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public void setCurrentPageNumber(int currentPageNumber) {
        this.currentPageNumber = currentPageNumber;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var curMessageForEdit = update.getCallbackQuery();
        /**
         * command with "/"
         */



        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/")){
            System.out.println("**************************");
            System.out.println(update.getMessage().getText() + " |command with \"/\"|  " + update.getMessage().getChatId());
            System.out.println("**************************");
            String messageText  = update.getMessage().getText();
            long chatId         = update.getMessage().getChatId();
            System.out.println("Command " + messageText);
            switch (messageText) {
                case START_COMMAND          -> createStartCommand(update, chatId);
                case ADD_COMMAND            -> createAddCommand(chatId);
                case MY_FOLDERS_COMMAND     -> createMyFoldersCommand(chatId);
                case MY_CALENDAR_COMMAND    -> createMyCalendarCommand(chatId);
                default                     -> defaultAnswer(chatId);
            }
            setLastCommand(chatId, messageText);

        }
        /**
         * command without "/"
         */
        else if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("**************************");
            System.out.println(update.getMessage().getText() + " |command without \"/\"|  " + update.getMessage().getChatId());
            System.out.println("**************************");
            String messageText  = update.getMessage().getText();
            long chatId         = update.getMessage().getChatId();
            System.out.println("Command without */* " + getLastCommand(chatId));
//            System.out.println("Command without */* " + messageText);
            switch (getLastCommand(chatId)) {
                case ENTER_EVENT_NAME_BUTTON            -> generateDoYouWantEnterEventDescriptionButton(chatId, messageText);
                case YES_ENTER_EVENT_DESCRIPTION_BUTTON -> eventAdd(chatId, 0, messageText);
                case ADD_FOLDER_BUTTON                  -> {

                    this.temporaryFolder = new Folder();
                    temporaryFolder.setName(messageText);
                    User user = userRepository.findById(chatId).isPresent() ?
                            userRepository.findById(chatId).get() : null;
                    temporaryFolder.setOwnerID(user);
                    System.out.println("temporaryFolder " + temporaryFolder.getOwnerID());
                    generateDoYouWantAddEventToFolderButton(chatId, messageText);

                }
                default             -> defaultAnswer(chatId);
            }
            setLastCommand(chatId, messageText);

            System.out.println(temporaryEvent);

        }
        /**
         * command with CallbackQuery  (update) without "/"
         */
        else if (update.hasCallbackQuery() && !curMessageForEdit.getData().startsWith("/")){
            String callbackData = curMessageForEdit.getData();

            System.out.println("**************************");
            System.out.println(callbackData + " |command with CallbackQuery  (update) without */*|  " + curMessageForEdit.getMessage().getChatId());
            System.out.println("**************************");


            long messageId      = curMessageForEdit.getMessage().getMessageId();
            long chatId         = curMessageForEdit.getMessage().getChatId();
            System.out.println("update without */* " + callbackData);
//            System.out.println(callbackData.matches(CHOOSE_DAY_OF_MONTH_BUTTON));

            if(callbackData.equals(ADD_FOLDER_BUTTON)){
                sendMessageEnterSomething(messageId, chatId, "название");


//                generateEventOrFolderButton(messageId, chatId, "Нету пока, отдохни, брат");
                setLastCommand(chatId, callbackData);

            } else if (callbackData.equals(ADD_EVENT_BUTTON)) {

                generateChooseMonthButton(messageId, chatId);

//                generateCalendar(chatId);
//                generateEventOrFolderMes(messageId, chatId, "И тут похуй");
                setLastCommand(chatId, callbackData);

            } else if (callbackData.matches(CHOOSE_MONTH_BUTTON)) {

                generateChooseDayOfMonthButton(messageId, chatId, callbackData);

                System.out.println("================================");
                setLastCommand(chatId, callbackData);

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
                System.out.println("temporaryEvent " + temporaryEvent.getOwnerID());

//                temporaryEvent.set
//                addEventMessage(messageId, chatId, callbackData);

                setLastCommand(chatId, ENTER_EVENT_NAME_BUTTON);
            }
//            else if (callbackData.startsWith(ENTER_EVENT_NAME_BUTTON)) {
//                sendMessageEnterSomething(messageId, chatId, "название");
//                setLastCommand(ENTER_EVENT_NAME_BUTTON);
//            }
            else if (callbackData.equals(YES_ENTER_EVENT_DESCRIPTION_BUTTON)) {
                sendMessageEnterSomething(messageId, chatId, "описание");
                setLastCommand(chatId, callbackData);
            }else if (callbackData.equals(NO_ENTER_EVENT_DESCRIPTION_BUTTON)) {
                eventAdd(chatId, messageId,"");
//                sendMessageEnterSomething(messageId, chatId, "описание");
                setLastCommand(chatId, callbackData);
            } else if (callbackData.equals(YES_ADD_EVENT_TO_FOLDER_BUTTON)) {
                generateChooseEventToFolderButton(messageId, chatId);
                setLastCommand(chatId, callbackData);
            } else if (callbackData.startsWith(CHOOSE_EVENT_FOR_FOLDER_BUTTON)) {
                String[] split = callbackData.split("_");
                Integer index = Integer.parseInt(split[5]);

                if(!temporaryEventIndexList.contains(index))
                    temporaryEventIndexList.add(index);
                else
                    temporaryEventIndexList.remove(index);

                generateChooseEventToFolderButton(messageId, chatId);

            } else if (callbackData.startsWith(SWITCH_PAGE_BUTTON)) {

                String[] split = callbackData.split(":");
                System.out.println(Arrays.toString(split));
                setCurrentPageNumber(Integer.parseInt(split[2]));
                //смотрим откуда пришла команда, но добавляем ":" потому что split убирает эту штуку
                String position = split[1] + ":";
                switch (position){
                    case IN_CHOOSE_EVENT_FOR_FOLDER -> generateChooseEventToFolderButton(messageId, chatId);
                    case IN_CHOOSE_FOLDER -> generateChooseFolderForWatchingEventsButton(messageId, chatId);
                }



            } else if (callbackData.equals(NO_ADD_EVENT_TO_FOLDER_BUTTON)) {
                folderAdd(chatId, messageId);

            } else if (callbackData.equals(ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON)){

                folderAdd(chatId, messageId);


            } else if (callbackData.startsWith(CHOOSE_FOLDER_BUTTON)) {
                String[] split = callbackData.split("_");

                //список событий, которым надо будет удалить folderId
                temporaryEventIndexList = new ArrayList<>();
                setCurrentPageNumber(0);
                generateEventInFolderButton(messageId, chatId, Long.parseLong(split[3]));
                setLastCommand(chatId, callbackData);

            }
            // getLastCommand() так как обращаемся к предыдущей команде
            else if (callbackData.startsWith(SAME_MESSAGE_BUTTON)) {
                String[] parts = callbackData.split("_");

                generateChooseDayOfMonthButton(Long.parseLong(parts[4]),
                        Long.parseLong(parts[3]),
                        getLastCommand(chatId));


            } else if(callbackData.startsWith(DELETE_EVENT_FROM_FOLDER_BUTTON)){

                String[] split = callbackData.split("_");
                Integer index = Integer.valueOf(split[5]);
                if(temporaryEventIndexList.contains(index))
                    temporaryEventIndexList.remove(index);
                else
                    temporaryEventIndexList.add(index);

                //Теперь достаем индекс папки, в которой работаем, из CHOOSE_FOLDER_BUTTON_ИНДЕКС
                index = Integer.parseInt(getLastCommand(chatId).split("_")[3]);
                generateEventInFolderButton(messageId, chatId, index);

            } else if (callbackData.startsWith(DELETE_FOLDER_BUTTON)) {

                String[] split = callbackData.split("_");
                int index = Integer.parseInt(split[3]);

                deleteFolder(index);

                createMyFoldersCommand(chatId);

            }


        }
        /**
         * command with CallbackQuery  (update) with "/"
         * для того, чтобы обработать кнопки назад на
         * главные команды, которые начинаются с "/".
         */
        else if (update.hasCallbackQuery() && curMessageForEdit.getData().startsWith("/")) {
            String callbackData = curMessageForEdit.getData();

            System.out.println("**************************");
            System.out.println(callbackData + " |command with CallbackQuery  (update) with */* |  " + curMessageForEdit.getMessage().getChatId());
            System.out.println("**************************");


            long messageId      = curMessageForEdit.getMessage().getMessageId();
            long chatId         = curMessageForEdit.getMessage().getChatId();
            System.out.println("update with */* " + callbackData);
            switch (callbackData){
                case MY_FOLDERS_COMMAND ->{
                    saveChangesInFolder();
                    createMyFoldersCommand(chatId);
                }
            }
//            if(callbackData.equals(MY_FOLDERS_COMMAND)){
//
//            }
        }


    }

    /**
     * START info message
     */
    private void sendMessageEventAdded(long chatId, long messageId){
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId((int) messageId);
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
    private void sendMessageEventAdded(long chatId){
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

    private void eventAdd(long chatId, long messageId, String eventDescription){
        temporaryEvent.setDescription(eventDescription);
        eventRepository.save(temporaryEvent);
//        temporaryEvent = new ;
        sendMessageEventAdded(chatId, messageId);
        if(messageId == 0)
            sendMessageEventAdded(chatId);
        else
            sendMessageEventAdded(chatId, messageId);
    }
    private void folderAdd(long chatId, long messageId){
        folderRepository.save(temporaryFolder);

        for(int index : temporaryEventIndexList){
            Event curEvent = eventRepository.findById((long) index).get();
            curEvent.setFolderID(temporaryFolder);
            eventRepository.save(curEvent);
        }

        EditMessageText message = new EditMessageText();
        message.setText("Папка <b>" + temporaryFolder.getName() + "</b> успешно добавлена. Обращайтесь!)");
        message.enableHtml(true);
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }
        temporaryEventIndexList = new ArrayList<>();
    }
    private void saveChangesInFolder(){

        for(int index : temporaryEventIndexList){
            Event curEvent = eventRepository.findById((long) index).get();
            curEvent.setFolderID(null);
            eventRepository.save(curEvent);
        }

    }
    private void deleteFolder(long folderId){
        Folder folder = folderRepository.findById(folderId).get();
        List<Event> eventList = eventRepository.findByFolderID(folder);

        for(Event e : eventList){
            e.setFolderID(null);
            eventRepository.save(e);
        }

        folderRepository.deleteById(folderId);



    }
    /**
     *  END info message
     *  -------------------------------
     *  START methods for compute something
     */
    private int iteratorSize(Iterator<Event> eventIterator){
        int count = 0;

        while (eventIterator.hasNext()){
            eventIterator.next();
            count++;
        }
        return count;
    }
    private List<Event> getEventListByUserId(long ownerId){
        User user = userRepository.findById(ownerId).get();
        return eventRepository.findByOwnerID(user);
    }
    private List<Event> getEventListByFolderId(long folderId){
        Folder folder = folderRepository.findById(folderId).get();
        return eventRepository.findByFolderID(folder);
    }
    /**
     *  END methods for compute something
     * -----------------------------------
     *  START input text processing
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
    /**
     * END input text processing
     * ---------------------------------
     * START button processing area
     */
    private void generateChooseEventToFolderButton(long messageId, long chatId){

        int pageNumber = getCurrentPageNumber();

        EditMessageText message = new EditMessageText();
        message.setText("Выбирайте!");
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var okButton = new InlineKeyboardButton();
        okButton.setText("ОК");
        okButton.setCallbackData(ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON);

        rowInline.add(okButton);
        rowsInline.add(rowInline);

        List<Event> eventList = getEventListByUserId(chatId);
        Iterator<Event> eventIterator = eventList.iterator();
        var eventIteratorSize = iteratorSize(eventRepository.findById(chatId).stream().iterator());
        String gray_check_mark = EmojiParser.parseToUnicode(":radio_button:");
        String green_check_mark = EmojiParser.parseToUnicode(":white_check_mark:");

        int curEventIndex = 0;

        while (eventIterator.hasNext() && curEventIndex < (pageNumber * 10)){
            eventIterator.next();
            curEventIndex++;
        }

        // задаю количество событий на странице
        int uppLimitEventIndex = curEventIndex + 10;

        while (eventIterator.hasNext() && curEventIndex < uppLimitEventIndex) {
            rowInline = new ArrayList<>();

            Event curEvent = eventIterator.next();

            var buttonEventName = new InlineKeyboardButton();
            buttonEventName.setText(curEvent.getName());
            buttonEventName.setCallbackData(YES_ADD_EVENT_TO_FOLDER_BUTTON);

            var buttonGreenCheckmark = new InlineKeyboardButton();
            if (!temporaryEventIndexList.contains((int)curEvent.getId()))
                buttonGreenCheckmark.setText(gray_check_mark);
            else
                buttonGreenCheckmark.setText(green_check_mark);
            buttonGreenCheckmark.setCallbackData(CHOOSE_EVENT_FOR_FOLDER_BUTTON + curEvent.getId());

            rowInline.add(buttonEventName);
            rowInline.add(buttonGreenCheckmark);
            rowsInline.add(rowInline);
            curEventIndex++;
        }

        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        if (pageNumber - 1 >= 0)
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + (pageNumber - 1));
        else
            previousPageButton.setCallbackData(YES_ADD_EVENT_TO_FOLDER_BUTTON);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((eventIteratorSize / 10) + ((eventIteratorSize % 10 == 0 && eventIteratorSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(YES_ADD_EVENT_TO_FOLDER_BUTTON);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)eventIteratorSize / 10 > (pageNumber+1) )
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_EVENT_FOR_FOLDER + (pageNumber + 1));
        else
            nextPageButton.setCallbackData(YES_ADD_EVENT_TO_FOLDER_BUTTON);

        rowInline = new ArrayList<>();
        rowInline.add(previousPageButton);
        rowInline.add(infoWhichPageButton);
        rowInline.add(nextPageButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }
    private void generateDoYouWantAddEventToFolderButton(long chatId, String folderName){

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Хотите добавить события в папку?");

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData(YES_ADD_EVENT_TO_FOLDER_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData(NO_ADD_EVENT_TO_FOLDER_BUTTON);

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }
    }
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

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

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


        int firstDayOfMonth = LocalDate.parse(month.matches("\\d") ? ("01.0" + month + ".2023")
                                              : ("01." + month + ".2023"),
                                              DateTimeFormatter.ofPattern("dd.MM.yyyy")).getDayOfWeek().getValue();

        String textForEmptyButton = EmojiParser.parseToUnicode(":wavy_dash:");
        InlineKeyboardButton emptyButton = new InlineKeyboardButton();
        emptyButton.setText(textForEmptyButton);
        emptyButton.setCallbackData(SAME_MESSAGE_BUTTON + chatId + "_" + messageId);
        // заполняем пустоты, подгоняем 1 число под день недели
        for(int i = 0; i < firstDayOfMonth - 1; i++){
            rowInline.add(emptyButton);
        }
        // заполняем весь месяц даты
        int i = 1;
        Month curMonth = Month.of(parseInt(month));
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
    private void generateEventOrFolderButton(long chatId){
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
    private void generateEventInFolderButton(long messageId, long chatId, long folderId){
        int pageNumber = getCurrentPageNumber();

        EditMessageText message = new EditMessageText();
        String folderName = folderRepository.findById(folderId).get().getName();
        message.setText("Ваши события \n в папке <b>" + folderName + "</b>");
        message.enableHtml(true);
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var goBackButton = new InlineKeyboardButton();
        String go_back_mark = EmojiParser.parseToUnicode(":back:");
        goBackButton.setText(go_back_mark);
        goBackButton.setCallbackData(MY_FOLDERS_COMMAND);

        var addEventButton = new InlineKeyboardButton();
        String add_event_mark = EmojiParser.parseToUnicode(":heavy_plus_sign:");
        addEventButton.setText(add_event_mark + " событие");
        addEventButton.setCallbackData(MY_FOLDERS_COMMAND);



        rowInline.add(goBackButton);
        rowInline.add(addEventButton);
        rowsInline.add(rowInline);

        List<Event> eventList = getEventListByFolderId(folderId);
        Iterator<Event> eventIterator = eventList.iterator();
        var eventListSize = eventList.size();
        String delete_event_mark = EmojiParser.parseToUnicode(":x:");
        add_event_mark = EmojiParser.parseToUnicode(":radio_button:");

        int curEventIndex = 0;

        while (eventIterator.hasNext() && curEventIndex < (pageNumber * 10)){
            eventIterator.next();
            curEventIndex++;
        }

        // задаю количество событий на странице
        int uppLimitEventIndex = curEventIndex + 10;

        while (eventIterator.hasNext() && curEventIndex < uppLimitEventIndex) {
            rowInline = new ArrayList<>();

            Event curEvent = eventIterator.next();

            var buttonEventName = new InlineKeyboardButton();
            buttonEventName.setText(curEvent.getName());
            buttonEventName.setCallbackData(INFO_ABOUT_EVENT_BUTTON);

            var buttonGreenCheckmark = new InlineKeyboardButton();
            if (!temporaryEventIndexList.contains((int)curEvent.getId()))
                buttonGreenCheckmark.setText(delete_event_mark);
            else
                buttonGreenCheckmark.setText(add_event_mark);
            buttonGreenCheckmark.setCallbackData(DELETE_EVENT_FROM_FOLDER_BUTTON + curEvent.getId());

            rowInline.add(buttonEventName);
            rowInline.add(buttonGreenCheckmark);
            rowsInline.add(rowInline);
            curEventIndex++;
        }

        var buttonDeleteFolder = new InlineKeyboardButton();
        String delete_folder_mark = EmojiParser.parseToUnicode("Удалить папку " + ":wastebasket:");
        buttonDeleteFolder.setText(delete_folder_mark);
        buttonDeleteFolder.setCallbackData(DELETE_FOLDER_BUTTON + folderId);
        rowInline = new ArrayList<>();
        rowInline.add(buttonDeleteFolder);
        rowsInline.add(rowInline);



        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        if (pageNumber - 1 >= 0)
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + (pageNumber - 1));
        else
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + pageNumber);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((eventListSize / 10) + ((eventListSize % 10 == 0 && eventListSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)eventListSize / 10 > (pageNumber+1) )
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_EVENT_FOR_FOLDER + (pageNumber + 1));
        else
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + pageNumber);

        rowInline = new ArrayList<>();
        rowInline.add(previousPageButton);
        rowInline.add(infoWhichPageButton);
        rowInline.add(nextPageButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }


    // у этих двоих выделить в отдельный метод общее
    private void generateChooseFolderForWatchingEventsButton(long messageId, long chatId){
        int pageNumber = getCurrentPageNumber();

        EditMessageText message = new EditMessageText();
        message.setText("Ваши папки. Нажмите для просмотра событий в папке.");
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

//        var okButton = new InlineKeyboardButton();
//        okButton.setText("ОК");
//        okButton.setCallbackData(ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON);

//        rowInline.add(okButton);
//        rowsInline.add(rowInline);

//        List<Event> eventList = getEventListByUserId(chatId);
//        Iterator<Event> eventIterator = eventList.iterator();
//        var eventIteratorSize = iteratorSize(eventRepository.findById(chatId).stream().iterator());
//        String gray_check_mark = EmojiParser.parseToUnicode(":radio_button:");
//        String green_check_mark = EmojiParser.parseToUnicode(":white_check_mark:");
        User curUser = userRepository.findById(chatId).get();
        List<Folder> folderList = folderRepository.findByOwnerID(curUser);
        Iterator<Folder> folderIterator = folderList.iterator();

        int curFolderIndex = 0;

        while (folderIterator.hasNext() && curFolderIndex < (pageNumber * 10)){
            folderIterator.next();
            curFolderIndex++;
        }

        // задаю количество событий на странице
        int uppLimitFolderIndex = curFolderIndex + 10;

        while (folderIterator.hasNext() && curFolderIndex < uppLimitFolderIndex) {
            rowInline = new ArrayList<>();

            Folder curFolder = folderIterator.next();

            var buttonFolderName = new InlineKeyboardButton();
            buttonFolderName.setText(curFolder.getName());
            buttonFolderName.setCallbackData(CHOOSE_FOLDER_BUTTON + curFolder.getId());
//
//            var buttonGreenCheckmark = new InlineKeyboardButton();
//            if (!temporaryEventIndexListForAddedToFolder.contains((int)curFolder.getId()))
//                buttonGreenCheckmark.setText(gray_check_mark);
//            else
//                buttonGreenCheckmark.setText(green_check_mark);
//            buttonGreenCheckmark.setCallbackData(CHOOSE_EVENT_FOR_FOLDER_BUTTON + curEvent.getId());

            rowInline.add(buttonFolderName);
//            rowInline.add(buttonGreenCheckmark);
            rowsInline.add(rowInline);
            curFolderIndex++;
        }

        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        if (pageNumber - 1 >= 0)
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + (pageNumber - 1));
        else
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + pageNumber);

        var folderListSize = folderList.size();
        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((folderListSize / 10) + ((folderListSize % 10 == 0 && folderListSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)folderListSize / 10 > (pageNumber+1) )
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + (pageNumber + 1));
        else
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + pageNumber);

        rowInline = new ArrayList<>();
        rowInline.add(previousPageButton);
        rowInline.add(infoWhichPageButton);
        rowInline.add(nextPageButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }
    private void generateChooseFolderForWatchingEventsButton(long chatId){
        int pageNumber = getCurrentPageNumber();

        SendMessage message = new SendMessage();
        message.setText("Ваши папки. Нажмите для просмотра событий в папке.");
        message.setChatId(chatId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

//        var okButton = new InlineKeyboardButton();
//        okButton.setText("ОК");
//        okButton.setCallbackData(ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON);

//        rowInline.add(okButton);
//        rowsInline.add(rowInline);

//        List<Event> eventList = getEventListByUserId(chatId);
//        Iterator<Event> eventIterator = eventList.iterator();
//        var eventIteratorSize = iteratorSize(eventRepository.findById(chatId).stream().iterator());
//        String gray_check_mark = EmojiParser.parseToUnicode(":radio_button:");
//        String green_check_mark = EmojiParser.parseToUnicode(":white_check_mark:");
        User curUser = userRepository.findById(chatId).get();
        List<Folder> folderList = folderRepository.findByOwnerID(curUser);
        Iterator<Folder> folderIterator = folderList.iterator();

        int curFolderIndex = 0;

        while (folderIterator.hasNext() && curFolderIndex < (pageNumber * 10)){
            folderIterator.next();
            curFolderIndex++;
        }

        // задаю количество событий на странице
        int uppLimitFolderIndex = curFolderIndex + 10;

        while (folderIterator.hasNext() && curFolderIndex < uppLimitFolderIndex) {
            rowInline = new ArrayList<>();

            Folder curFolder = folderIterator.next();

            var buttonFolderName = new InlineKeyboardButton();
            buttonFolderName.setText(curFolder.getName());
            buttonFolderName.setCallbackData(CHOOSE_FOLDER_BUTTON + curFolder.getId());



//
//            var buttonGreenCheckmark = new InlineKeyboardButton();
//            if (!temporaryEventIndexListForAddedToFolder.contains((int)curFolder.getId()))
//                buttonGreenCheckmark.setText(gray_check_mark);
//            else
//                buttonGreenCheckmark.setText(green_check_mark);
//            buttonGreenCheckmark.setCallbackData(CHOOSE_EVENT_FOR_FOLDER_BUTTON + curEvent.getId());

            rowInline.add(buttonFolderName);

//            rowInline.add(buttonGreenCheckmark);
            rowsInline.add(rowInline);
            curFolderIndex++;
        }

        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        if (pageNumber - 1 >= 0)
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + (pageNumber - 1));
        else
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + pageNumber);

        var folderListSize = folderList.size();
        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((folderListSize / 10) + ((folderListSize % 10 == 0 && folderListSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)folderListSize / 10 > (pageNumber+1) )
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + (pageNumber + 1));
        else
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHOOSE_FOLDER + pageNumber);

        rowInline = new ArrayList<>();
        rowInline.add(previousPageButton);
        rowInline.add(infoWhichPageButton);
        rowInline.add(nextPageButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
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
    private void createMyFoldersCommand(long chatId){
        setCurrentPageNumber(0);
        generateChooseFolderForWatchingEventsButton(chatId);


    }
    private void createAddCommand(long chatId){

        generateEventOrFolderButton(chatId);

    }

    /**
     *  END command processing area
     */

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

    }
}
