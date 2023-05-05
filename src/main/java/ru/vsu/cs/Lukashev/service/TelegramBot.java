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
import ru.vsu.cs.Lukashev.entity.*;
import ru.vsu.cs.Lukashev.repository.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String YES_ADD_EVENT_TO_FOLDER_BUTTON          = "YES_ADD_EVENT_TO_FOLDER_BUTTON_";
    private static final String NO_ADD_EVENT_TO_FOLDER_BUTTON           = "NO_ADD_EVENT_TO_FOLDER_BUTTON";
    private static final String CHOOSE_EVENT_FOR_FOLDER_BUTTON          = "CHOOSE_EVENT_FOR_FOLDER_BUTTON_";
    private static final String SWITCH_PAGE_BUTTON                      = "SWITCH_PAGE_BUTTON:";
    private static final String ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON = "ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON_";
    private static final String ADD_FOLDER_WHICH_CHOOSE_TO_SUBSCRIBER_BUTTON = "ADD_FOLDER_WHICH_CHOOSE_TO_SUBSCRIBER_BUTTON:";
    private static final String CHOOSE_FOLDER_BUTTON                    = "CHOOSE_FOLDER_BUTTON_";
    private static final String DELETE_EVENT_FROM_FOLDER_BUTTON         = "DELETE_EVENT_FROM_FOLDER_BUTTON_";
    private static final String INFO_ABOUT_EVENT_BUTTON                 = "INFO_ABOUT_EVENT_BUTTON_";
    private static final String DELETE_FOLDER_BUTTON                    = "DELETE_FOLDER_BUTTON_";
    private static final String SAVE_CHANGES_IN_FOLDER_BUTTON           = "SAVE_CHANGES_IN_FOLDER_BUTTON";
    private static final String CHECK_SUBSCRIBERS_BUTTON                = "CHECK_SUBSCRIBERS_BUTTON";
    private static final String CHECK_SUBSCRIPTIONS_BUTTON              = "CHECK_SUBSCRIPTIONS_BUTTON";
    private static final String CHECK_SUBSCRIPTIONS_BY_FOLDERS_BUTTON   = "CHECK_SUBSCRIPTIONS_BY_FOLDERS_BUTTON";
    private static final String CHECK_SUBSCRIBERS_BY_FOLDERS_BUTTON     = "CHECK_SUBSCRIBERS_BY_FOLDERS_BUTTON";
    private static final String CHECK_SUBSCRIPTIONS_BY_LINKS_BUTTON     = "CHECK_SUBSCRIPTIONS_BY_LINKS_BUTTON";
    private static final String CHECK_SUBSCRIBERS_BY_LINKS_BUTTON       = "CHECK_SUBSCRIBERS_BY_LINKS_BUTTON";
    private static final String ADD_SUBSCRIBER_BUTTON                   = "ADD_SUBSCRIBER_BUTTON";
    private static final String ZERO_ACTION_BUTTON                      = "ZERO_ACTION_BUTTON";
    private static final String YES_CONFIRM_BUTTON                      = "YES_CONFIRM_BUTTON:";
    private static final String CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON     = "CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON:";
    private static final String ADD_FOLDER_FOR_SUBSCRIBER_BUTTON        = "ADD_FOLDER_FOR_SUBSCRIBER_BUTTON:";
    private static final String DELETE_FOLDER_FROM_PERMISSION_BUTTON    = "DELETE_FOLDER_FROM_PERMISSION_BUTTON_";
    private static final String CHOOSE_FOLDER_FOR_SUBSCRIBER_BUTTON     = "CHOOSE_FOLDER_FOR_SUBSCRIBER_BUTTON:";
    private static final String PREVIOUS_PAGE                           = "PREVIOUS_PAGE";
    private static final String DELETE_SUBSCRIBER_BUTTON                = "DELETE_SUBSCRIBER_BUTTON:";


    //POSITIONS
    private static final String IN_CHOOSE_EVENT_FOR_FOLDER              = "IN_CHOOSE_EVENT_FOR_FOLDER:";
    private static final String IN_CHOOSE_FOLDER                        = "IN_CHOOSE_FOLDER:";
    private static final String IN_CHOOSE_EVENT                         = "IN_CHOOSE_EVENT:";
    private static final String IN_CHECK_SUBSCRIBERS_BY_FOLDERS         = "IN_CHOOSE_EVENT:";
    private static final String IN_CHECK_SUBSCRIPTIONS_BY_FOLDERS       = "IN_CHOOSE_EVENT:";
    private static final String IN_CHECK_SUBSCRIPTIONS_BY_LINKS         = "IN_CHOOSE_EVENT:";
    private static final String IN_CHECK_SUBSCRIBERS_BY_LINKS_BUTTON    = "IN_CHOOSE_EVENT:";
    private static final String IN_CHECK_FOLDERS_FOR_SUBSCRIBER         = "IN_CHECK_FOLDERS_FOR_SUBSCRIBER:";





//    private User temporaryUser;
    private Folder temporaryFolder;
//    private Perm temporaryUser;
    private Event temporaryEvent;

    private List<Integer> temporaryIndexList = new ArrayList<>();

    private int currentPageNumber = 0;



    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private ConfirmationOfSubscribeRepository confirmationOfSubscribeRepository;

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

    public List<Integer> getTemporaryIndexList() {
        return temporaryIndexList;
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
//        System.out.println(update.getMessage().getChat().getUserName());

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/")){
            System.out.println("**************************");
            System.out.println(update.getMessage().getText() + " |command with \"/\"|  " + update.getMessage().getChatId());
            System.out.println("**************************");
            String messageText  = update.getMessage().getText();
            long chatId         = update.getMessage().getChatId();
            System.out.println("Last command:" + getLastCommand(chatId));

            System.out.println("Command " + messageText);
            switch (messageText) {
                case START_COMMAND          -> createStartCommand(update, chatId);
                case ADD_COMMAND            -> createAddCommand(chatId);
                case MY_FOLDERS_COMMAND     -> createMyFoldersCommand(chatId);
                case MY_CALENDAR_COMMAND    -> createMyCalendarCommand(chatId);
                case PERMISSIONS_COMMAND    -> createPermissionCommand(chatId);
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
            System.out.println("Last command:" + getLastCommand(chatId));

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
                case ADD_SUBSCRIBER_BUTTON              -> {
                    User subscriber = userRepository.findByLink(messageText);
                    User owner      = userRepository.findById(chatId).get();
                    ConfirmationOfSubscribe conf =
                            confirmationOfSubscribeRepository.
                                    findByOwnerIDAndSubscriberID(owner, subscriber);
                    if(conf == null){
                        conf = new ConfirmationOfSubscribe();
                        conf.setOwnerID(owner);
                        conf.setSubscriberID(subscriber);
                        conf.setConfirm(false);
                        confirmationOfSubscribeRepository.save(conf);
                        if (!generateDoYouWantSubscribe(subscriber.getId(), owner.getLink())){
//                            System.out.println("добавил");
                            sendMessage(chatId, "Пользователь с таким ником не зарегистрирован у нашего бота!");
                        }
                    } else if (!conf.isConfirm()) {
                        if (!generateDoYouWantSubscribe(subscriber.getId(), owner.getLink())){
//                            System.out.println("добавил");
                            sendMessage(chatId, "Пользователь с таким ником не зарегистрирован у нашего бота!");
                        }
                    } else
                    {
                        sendMessage(chatId, "@" + messageText + " уже подписан на вас!");
                    }

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
            System.out.println("Last command:" + getLastCommand(chatId));

            System.out.println("update without */* " + callbackData);
//            System.out.println(callbackData.matches(CHOOSE_DAY_OF_MONTH_BUTTON));

            //Проверям не была ли нажата кнопка *назад*
//            if (callbackData.equals(PREVIOUS_PAGE)){
//
//            }


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
            }
            else if (callbackData.startsWith(YES_ADD_EVENT_TO_FOLDER_BUTTON)) {
                this.temporaryIndexList = new ArrayList<>();

                String[] split = callbackData.split("_");
                long folderIndex = Long.valueOf(split[6]);
                addEventToTemporaryList(folderIndex);
                this.temporaryFolder = folderRepository.findById(folderIndex).get();
                generateChooseEventToFolderButton(messageId, chatId);
                setLastCommand(chatId, callbackData);
            }
            else if (callbackData.startsWith(CHOOSE_EVENT_FOR_FOLDER_BUTTON)) {
                String[] split = callbackData.split("_");
                Integer index = Integer.parseInt(split[5]);

                if(!temporaryIndexList.contains(index))
                    temporaryIndexList.add(index);
                else
                    temporaryIndexList.remove(index);

                generateChooseEventToFolderButton(messageId, chatId);

            }
            else if (callbackData.startsWith(CHOOSE_FOLDER_FOR_SUBSCRIBER_BUTTON)) {
                String[] split = callbackData.split(":");
                Integer index = Integer.parseInt(split[1]);
                String link = getLastCommand(chatId).split(":")[1];
                User subscriber = userRepository.findByLink(link);
                if(!temporaryIndexList.contains(index))
                    temporaryIndexList.add(index);
                else
                    temporaryIndexList.remove(index);
                System.out.println("ыыыыыыыыыыыыыыыыыыыы" + temporaryIndexList);
                generateChooseFoldersForSubscriberButton(chatId, messageId, subscriber);

            }else if (callbackData.startsWith(SWITCH_PAGE_BUTTON)) {

                String[] split = callbackData.split(":");
                System.out.println(Arrays.toString(split));
                setCurrentPageNumber(Integer.parseInt(split[2]));
                //смотрим откуда пришла команда, но добавляем ":" потому что split убирает эту штуку
                String position = split[1] + ":";
                switch (position){
                    case IN_CHOOSE_EVENT_FOR_FOLDER                 -> generateChooseEventToFolderButton(messageId, chatId);
                    case IN_CHOOSE_FOLDER                           -> generateChooseFolderForWatchingEventsButton(messageId, chatId);
                    case IN_CHECK_SUBSCRIBERS_BY_LINKS_BUTTON       -> generateSubscribersByLinksButton(messageId, chatId);
                    case IN_CHECK_FOLDERS_FOR_SUBSCRIBER            -> {
                        split = getLastCommand(chatId).split(":");
                        User sub = userRepository.findById(Long.valueOf(split[1])).get();
                        User owner = userRepository.findById(chatId).get();
                        generateFoldersAtSubscriberButton(messageId, chatId, sub, owner);
                    }
                }



            } else if (callbackData.equals(NO_ADD_EVENT_TO_FOLDER_BUTTON)) {
                folderAdd(chatId, messageId);

            } else if (callbackData.equals(ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON)){

                folderAdd(chatId, messageId);


            }else if (callbackData.equals(ADD_FOLDER_WHICH_CHOOSE_TO_SUBSCRIBER_BUTTON)){

                System.out.println("333333333333333333333333" + getLastCommand(chatId));
                String link = getLastCommand(chatId).split(":")[1];
                addFoldersToSubscriber(link, chatId);

                generateSubscribersByLinksButton(messageId, chatId);


            } else if (callbackData.startsWith(CHOOSE_FOLDER_BUTTON)) {
                String[] split = callbackData.split("_");

                //список событий, которым надо будет удалить folderId
                temporaryIndexList = new ArrayList<>();
                setCurrentPageNumber(0);
                generateEventInFolderButton(messageId, chatId, Long.parseLong(split[3]), "");
                setLastCommand(chatId, callbackData);

            } else if (callbackData.startsWith(INFO_ABOUT_EVENT_BUTTON)) {
                String[] split = callbackData.split("_");

                long eventId = Long.parseLong(split[4]);

                split = getLastCommand(chatId).split("_");

                String eventInfo = toStringInfoAboutEvent(eventId);
                generateEventInFolderButton(messageId, chatId, Long.parseLong(split[3]), eventInfo);



//                generateEventInFolderButton(messageId, chatId, )



            }
            // getLastCommand() так как обращаемся к предыдущей команде
            else if (callbackData.startsWith(SAME_MESSAGE_BUTTON)) {
                String[] parts = callbackData.split("_");

                generateChooseDayOfMonthButton(Long.parseLong(parts[4]),
                        Long.parseLong(parts[3]),
                        getLastCommand(chatId));


            } else if (callbackData.equals(ZERO_ACTION_BUTTON)) {
                sendMessage(chatId, messageId, "ок");

            } else if(callbackData.startsWith(DELETE_EVENT_FROM_FOLDER_BUTTON)){

                String[] split = callbackData.split("_");
                Integer index = Integer.valueOf(split[5]);
                if(temporaryIndexList.contains(index))
                    temporaryIndexList.remove(index);
                else
                    temporaryIndexList.add(index);

                //Теперь достаем индекс папки, в которой работаем, из CHOOSE_FOLDER_BUTTON_ИНДЕКС
                index = Integer.parseInt(getLastCommand(chatId).split("_")[3]);
                generateEventInFolderButton(messageId, chatId, index, "");

            }else if(callbackData.startsWith(DELETE_FOLDER_FROM_PERMISSION_BUTTON)){

                String[] split = callbackData.split("_");
                Integer index = Integer.valueOf(split[5]);
                if(temporaryIndexList.contains(index))
                    temporaryIndexList.remove(index);
                else
                    temporaryIndexList.add(index);

                System.out.println(temporaryIndexList);
                //Теперь достаем индекс папки, в которой работаем, из CHOOSE_FOLDER_BUTTON_ИНДЕКС
                System.out.println(getLastCommand(chatId));
                User sub = userRepository.findByLink(getLastCommand(chatId).split(":")[1]);
                User owner = userRepository.findById(chatId).get();
                generateFoldersAtSubscriberButton(chatId, messageId, sub, owner);

            }
            else if (callbackData.startsWith(DELETE_FOLDER_BUTTON)) {

                String[] split = callbackData.split("_");
                int index = Integer.parseInt(split[3]);

                deleteFolder(index);

                createMyFoldersCommand(chatId);

            } else if (callbackData.startsWith(YES_CONFIRM_BUTTON)) {
                String[] split = callbackData.split(":");
                String link = split[1];

                User owner = userRepository.findByLink(link);
                User sub = userRepository.findById(chatId).get();
                ConfirmationOfSubscribe conf =
                        confirmationOfSubscribeRepository.findByOwnerIDAndSubscriberID(owner, sub);
                conf.setConfirm(true);
//                Permission p = new Permission();
                confirmationOfSubscribeRepository.save(conf);
                sendMessage(chatId, messageId, "ок");
                sendMessage(conf.getOwnerID().getId(), "@" + conf.getSubscriberID().getLink() + " теперь подписан на вас!");

            } else if (callbackData.equals(CHECK_SUBSCRIBERS_BUTTON)) {
                generateCheckSubscribersByLinksOrByFoldersButton(messageId, chatId);
                setLastCommand(chatId, callbackData);
            } else if (callbackData.equals(CHECK_SUBSCRIPTIONS_BUTTON)) {
                generateCheckSubscriptionsByLinksOrByFoldersButton(messageId, chatId);
                setLastCommand(chatId, callbackData);
            } else if (callbackData.equals(CHECK_SUBSCRIBERS_BY_LINKS_BUTTON)) {
//                if(getLastCommand(chatId).startsWith(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON)) {
                if (!temporaryIndexList.isEmpty() || getLastCommand(chatId).startsWith(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON)){
                    String link = getLastCommand(chatId).split(":")[1];
                    addFoldersToSubscriber(link, chatId);

                }


                generateSubscribersByLinksButton(messageId, chatId);
                setLastCommand(chatId, callbackData);
            } else if (callbackData.equals(ADD_SUBSCRIBER_BUTTON)) {
                sendMessage(chatId, "Введите ник пользователя, которому хотите отправить запрос ");
                setLastCommand(chatId, callbackData);

            } else if (callbackData.startsWith(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON)) {
                String[] split = callbackData.split(":");
                String link = split[1];
                User sub = userRepository.findByLink(link);
                User owner = userRepository.findById(chatId).get();
                addFolderToTemporaryList(link, chatId);
                generateFoldersAtSubscriberButton(chatId, messageId, sub, owner);
//                temporaryIndexList = new ArrayList<>();

                setLastCommand(chatId, callbackData);
            } else if (callbackData.startsWith(ADD_FOLDER_FOR_SUBSCRIBER_BUTTON)) {

                String[] split = callbackData.split(":");
                String link = split[1];
                addFolderToTemporaryList(link, chatId);
                System.out.println("addFolderToTemporaryList(link, chatId); " + temporaryIndexList);
                User sub = userRepository.findByLink(link);
                generateChooseFoldersForSubscriberButton(chatId, messageId, sub);
                setLastCommand(chatId, callbackData);

            } else if (callbackData.startsWith(DELETE_SUBSCRIBER_BUTTON)) {

                String[] split = callbackData.split(":");
                String link = split[1];
                deleteSubscriber(link, chatId);

                generateSubscribersByLinksButton(messageId, chatId);


            } else if(callbackData.equals(CHECK_SUBSCRIBERS_BY_FOLDERS_BUTTON)){

                if (!temporaryIndexList.isEmpty() || getLastCommand(chatId).startsWith(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON)){
                    String link = getLastCommand(chatId).split(":")[1];
                    addFoldersToSubscriber(link, chatId);

                }


//                generateSubscribersByFolderButton(messageId, chatId);
                setLastCommand(chatId, callbackData);



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
            System.out.println("Last command:" + getLastCommand(chatId));

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


    private String toStringInfoAboutEvent(long eventId){
        Event event = eventRepository.findById(eventId).get();
        String split = "-----------------";
        String description = event.getDescription() == null ? "" : event.getDescription() + "\n";

        return split + "\n"
                + "Название: " + event.getName() + "\n"
                + description
                + "Дата: " + event.getDate();
    }
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
    private void sendMessageFolderAdded(long chatId, long messageId){
        EditMessageText message = new EditMessageText();
        message.setText("Папка <b>" + temporaryFolder.getName() + "</b> успешно сохранена. Обращайтесь!)");
        message.enableHtml(true);
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }
    }
    private void addEventToFolder(long folderId){

        Folder folder = folderRepository.findById(folderId).get();

        for(int index : temporaryIndexList){
            Event curEvent = eventRepository.findById((long) index).get();
            curEvent.setFolderID(folder);
            eventRepository.save(curEvent);
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
    private void addFolderToSub(long chatId, long messageId, String eventDescription){


    }
    private void folderAdd(long chatId, long messageId){
        folderRepository.save(temporaryFolder);

        addEventToFolder(temporaryFolder.getId());

        sendMessageFolderAdded(chatId, messageId);

        temporaryIndexList = new ArrayList<>();
    }

    /**
     *  END info message
     *  -------------------------------
     *  START methods for compute something
     */


    private void addFolderToSubscriber(String username){
        User user = userRepository.findByLink(username);


    }
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
    private List<Folder> getFolderListBySubscriberAndOwner(User sub, User owner){
//        User sub = userRepository.findById(subId).get();
        List<Permission> permissionList = permissionRepository.findBySubscriberIDAndOwnerID(sub, owner);
        List<Folder> folderList = new ArrayList<>();
        for(Permission p : permissionList){
//            Folder folder = folderRepository.findById()
            folderList.add(p.getFolderID());
        }
        return folderList;
    }
    private void saveChangesInFolder(){

        for(int index : temporaryIndexList){
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
    private void addEventToTemporaryList(long folderId){
        Folder folder = folderRepository.findById(folderId).get();
        List<Event> eventList = eventRepository.findByFolderID(folder);
        this.temporaryIndexList = new ArrayList<>();
        for(Event event : eventList){
            temporaryIndexList.add((int) event.getId());
        }
    }
    private void addFolderToTemporaryList(String link, long ownerId){
//        Folder folder = folderRepository.findById(folderId).get();
//        List<Event> eventList = eventRepository.findByFolderID(folder);
        User sub = userRepository.findByLink(link);
        User owner = userRepository.findById(ownerId).get();
        List<Folder> folderList = getFolderListBySubscriberAndOwner(sub, owner);
        this.temporaryIndexList = new ArrayList<>();

        for(Folder f : folderList){
            temporaryIndexList.add((int) f.getId());
        }

//        for(Event event : eventList){
//            temporaryIndexList.add((int) event.getId());
//        }
    }
    private void addFoldersToSubscriber(String link, long ownerId){
        User sub = userRepository.findByLink(link);
        User owner = userRepository.findById(ownerId).get();

        List<Permission> permissionList = permissionRepository.findBySubscriberIDAndOwnerID(sub, owner);
        for(Permission perm : permissionList){
            Integer folderId = (int) perm.getFolderID().getId();
            if(!temporaryIndexList.contains(folderId)){
                permissionRepository.delete(perm);
                temporaryIndexList.remove(folderId);
            }
        }

        var tempListLong = temporaryIndexList.stream().map(Integer::longValue).toList();
//        Iterable<Folder> folderIterable = tempListLong.iterator();
        List<Folder> folderList = folderRepository.findAllByIdIn(tempListLong);
        for(Folder f : folderList){
            Permission p = new Permission();
            p.setOwnerID(owner);
            p.setSubscriberID(sub);
            p.setFolderID(f);
            permissionRepository.save(p);
        }

    }
    private List<String> getSubscribersIdByOwnerId(long ownerId){

        User owner = userRepository.findById(ownerId).get();
        List<ConfirmationOfSubscribe> confList = confirmationOfSubscribeRepository.findByOwnerIDAndIsConfirm(owner, true);
        List<String> subscribersId = new ArrayList<>();
        for(ConfirmationOfSubscribe conf : confList) {
            subscribersId.add(conf.getSubscriberID().getLink());
        }
        return subscribersId;
    }
    private void deleteSubscriber(String link, Long ownerId){
        User sub = userRepository.findByLink(link);
        User owner = userRepository.findById(ownerId).get();
        ConfirmationOfSubscribe confOfSub = confirmationOfSubscribeRepository.
                findByOwnerIDAndSubscriberIDAndIsConfirm(owner, sub, false);
        confOfSub.setConfirm(false);
        confirmationOfSubscribeRepository.save(confOfSub);
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
//    private void generateSubscribersByFolderButton(long messageId, long chatId){
//        int pageNumber = getCurrentPageNumber();
//
//        EditMessageText message = new EditMessageText();
//        String textForMes = "Ваши папки, к которым есть доступ";
//
//        message.setText(textForMes);
//
//        message.enableHtml(true);
//        message.setChatId(chatId);
//        message.setMessageId((int) messageId);
//
//        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//        List<InlineKeyboardButton> rowInline        = new ArrayList<>();
//
//        var addPersonButton = new InlineKeyboardButton();
//        String add_person_mark = EmojiParser.parseToUnicode(":heavy_plus_sign:");
//        addPersonButton.setText(add_person_mark + " папку");
//        addPersonButton.setCallbackData(ADD_FOLDER_FOR_BUTTON);
//
//        rowInline.add(addPersonButton);
//        rowsInline.add(rowInline);
//
//        List<String> subscribersLinkList = getSubscribersIdByOwnerId(chatId);
//        Iterator<String> linkListIterator = subscribersLinkList.iterator();
//        var subscribersLinkListSize = subscribersLinkList.size();
//        String delete_event_mark = EmojiParser.parseToUnicode(":x:");
//
//        int curEventIndex = 0;
//
//        while (linkListIterator.hasNext() && curEventIndex < (pageNumber * 10)){
//            linkListIterator.next();
//            curEventIndex++;
//        }
//
//        // задаю количество на странице
//        int uppLimitLinkUserIndex = curEventIndex + 10;
//
//        while (linkListIterator.hasNext() && curEventIndex < uppLimitLinkUserIndex) {
//            rowInline = new ArrayList<>();
//
//            String curSubLink = linkListIterator.next();
//
//            var linkSubButton = new InlineKeyboardButton();
//            linkSubButton.setText(curSubLink);
//            linkSubButton.setCallbackData(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON + curSubLink);
//
//            var addFolderForSubButton = new InlineKeyboardButton();
//            addFolderForSubButton.setText("Добавить папку");
////            addFolderForSubButton.setCallbackData("добавляем папку сабу");
//            addFolderForSubButton.setCallbackData(ADD_FOLDER_FOR_SUBSCRIBER_BUTTON + curSubLink);
//
//            var unsubscribeButton = new InlineKeyboardButton();
//            unsubscribeButton.setText("Отписать");
////            unsubscribeButton.setCallbackData("Отписываем чела");
//            unsubscribeButton.setCallbackData(DELETE_SUBSCRIBER_BUTTON + curSubLink);
//
//            rowInline.add(linkSubButton);
//            rowInline.add(addFolderForSubButton);
//            rowInline.add(unsubscribeButton);
//            rowsInline.add(rowInline);
//            curEventIndex++;
//        }
//
//        var previousPageButton = new InlineKeyboardButton();
//        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
//        previousPageButton.setText(arrow_left);
//
//        if (pageNumber - 1 >= 0)
//            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + (pageNumber - 1));
//        else
//            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + pageNumber);
//
//        var infoWhichPageButton = new InlineKeyboardButton();
//        infoWhichPageButton.setText((pageNumber + 1) + "/" +
//                ((subscribersLinkListSize / 10) + ((subscribersLinkListSize % 10 == 0 && subscribersLinkListSize > 10)
//                        ? 0 : 1)));
//        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + pageNumber);
//
//        var nextPageButton = new InlineKeyboardButton();
//        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
//        nextPageButton.setText(arrow_right);
//        //проверяем есть ли другие страницы
//        if ((double)subscribersLinkListSize / 10 > (pageNumber+1) )
//            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHECK_SUBSCRIBERS_BY_FOLDERS + (pageNumber + 1));
//        else
//            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + pageNumber);
//
//        rowInline = new ArrayList<>();
//        rowInline.add(previousPageButton);
//        rowInline.add(infoWhichPageButton);
//        rowInline.add(nextPageButton);
//        rowsInline.add(rowInline);
//
//        markupInline.setKeyboard(rowsInline);
//
//        message.setReplyMarkup(markupInline);
//
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//
//        }
//
//    }
    private void generateChooseEventToFolderButton(long messageId, long chatId, long folderId){

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
        okButton.setCallbackData(ADD_EVENT_WHICH_CHOOSE_TO_FOLDER_BUTTON + folderId);

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
            if (!temporaryIndexList.contains((int)curEvent.getId()))
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
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + pageNumber);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((eventIteratorSize / 10) + ((eventIteratorSize % 10 == 0 && eventIteratorSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)eventIteratorSize / 10 > (pageNumber+1) )
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
            if (!temporaryIndexList.contains((int)curEvent.getId()))
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
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + pageNumber);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((eventIteratorSize / 10) + ((eventIteratorSize % 10 == 0 && eventIteratorSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHOOSE_EVENT_FOR_FOLDER + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)eventIteratorSize / 10 > (pageNumber+1) )
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
    private void generateEventInFolderButton(long messageId, long chatId, long folderId, String eventInfo){
        int pageNumber = getCurrentPageNumber();

        EditMessageText message = new EditMessageText();
        String folderName = folderRepository.findById(folderId).get().getName();
        String textForMes = "Ваши события \n в папке <b>" + folderName + "</b>. \n";

        if(!eventInfo.isEmpty())
            message.setText(textForMes + eventInfo);
        else
            message.setText(textForMes);

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
        addEventButton.setCallbackData(YES_ADD_EVENT_TO_FOLDER_BUTTON + folderId);



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
            buttonEventName.setCallbackData(INFO_ABOUT_EVENT_BUTTON + curEvent.getId());

            var buttonGreenCheckmark = new InlineKeyboardButton();
            if (!temporaryIndexList.contains((int)curEvent.getId()))
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


    private void generateSubscriptionsOrSubscribersButton(long chatId){

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Что хочешь");

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();
        var checkSubscriptionsButton                = new InlineKeyboardButton();

        checkSubscriptionsButton.setText("Подписки");
        checkSubscriptionsButton.setCallbackData(CHECK_SUBSCRIPTIONS_BUTTON);

        var checkSubscribersButton = new InlineKeyboardButton();

        checkSubscribersButton.setText("Подписчики");
        checkSubscribersButton.setCallbackData(CHECK_SUBSCRIBERS_BUTTON);

        rowInline.add(checkSubscribersButton);
        rowInline.add(checkSubscriptionsButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }


    }
    private void generateCheckSubscriptionsByLinksOrByFoldersButton(long messageId, long chatId){

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId((int) messageId);
        message.setText("Как хочешь");

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();
        var checkSubscriptionsButton                = new InlineKeyboardButton();

        checkSubscriptionsButton.setText("По никам");
        checkSubscriptionsButton.setCallbackData(CHECK_SUBSCRIPTIONS_BY_LINKS_BUTTON);

        var checkSubscribersButton = new InlineKeyboardButton();

        checkSubscribersButton.setText("По папкам");
        checkSubscribersButton.setCallbackData(CHECK_SUBSCRIPTIONS_BY_FOLDERS_BUTTON);

//        rowInline.add(checkSubscribersButton); TODO на диплом
        rowInline.add(checkSubscriptionsButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }


    }
    private void generateCheckSubscribersByLinksOrByFoldersButton(long messageId, long chatId){

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId((int) messageId);
        message.setText("Как хочешь");

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();
        var checkSubByLinksButton                   = new InlineKeyboardButton();

        checkSubByLinksButton.setText("По никам");
        checkSubByLinksButton.setCallbackData(CHECK_SUBSCRIBERS_BY_LINKS_BUTTON);

        var checkSubByFoldersButton = new InlineKeyboardButton();

        checkSubByFoldersButton.setText("По папкам");
        checkSubByFoldersButton.setCallbackData(CHECK_SUBSCRIBERS_BY_FOLDERS_BUTTON);

        rowInline.add(checkSubByLinksButton);
        rowInline.add(checkSubByFoldersButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }


    }

    public void generateSubscribersByLinksButton(long messageId, long chatId){
        int pageNumber = getCurrentPageNumber();

        EditMessageText message = new EditMessageText();
        String textForMes = "Ваши подписчики";

        message.setText(textForMes);

        message.enableHtml(true);
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var addPersonButton = new InlineKeyboardButton();
        String add_person_mark = EmojiParser.parseToUnicode(":heavy_plus_sign:");
        addPersonButton.setText(add_person_mark + " подписчика");
        addPersonButton.setCallbackData(ADD_SUBSCRIBER_BUTTON);

        rowInline.add(addPersonButton);
        rowsInline.add(rowInline);

        List<String> subscribersLinkList = getSubscribersIdByOwnerId(chatId);
        Iterator<String> linkListIterator = subscribersLinkList.iterator();
        var subscribersLinkListSize = subscribersLinkList.size();
        String delete_event_mark = EmojiParser.parseToUnicode(":x:");

        int curEventIndex = 0;

        while (linkListIterator.hasNext() && curEventIndex < (pageNumber * 10)){
            linkListIterator.next();
            curEventIndex++;
        }

        // задаю количество на странице
        int uppLimitLinkUserIndex = curEventIndex + 10;

        while (linkListIterator.hasNext() && curEventIndex < uppLimitLinkUserIndex) {
            rowInline = new ArrayList<>();

            String curSubLink = linkListIterator.next();

            var linkSubButton = new InlineKeyboardButton();
            linkSubButton.setText(curSubLink);
            linkSubButton.setCallbackData(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON + curSubLink);

            var addFolderForSubButton = new InlineKeyboardButton();
            addFolderForSubButton.setText("Добавить папку");
//            addFolderForSubButton.setCallbackData("добавляем папку сабу");
            addFolderForSubButton.setCallbackData(ADD_FOLDER_FOR_SUBSCRIBER_BUTTON + curSubLink);

            var unsubscribeButton = new InlineKeyboardButton();
            unsubscribeButton.setText("Отписать");
//            unsubscribeButton.setCallbackData("Отписываем чела");
            unsubscribeButton.setCallbackData(DELETE_SUBSCRIBER_BUTTON + curSubLink);

            rowInline.add(linkSubButton);
            rowInline.add(addFolderForSubButton);
            rowInline.add(unsubscribeButton);
            rowsInline.add(rowInline);
            curEventIndex++;
        }

        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        if (pageNumber - 1 >= 0)
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + (pageNumber - 1));
        else
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + pageNumber);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((subscribersLinkListSize / 10) + ((subscribersLinkListSize % 10 == 0 && subscribersLinkListSize > 10)
                        ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)subscribersLinkListSize / 10 > (pageNumber+1) )
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHECK_SUBSCRIBERS_BY_FOLDERS + (pageNumber + 1));
        else
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_SUBSCRIBERS_BY_FOLDERS + pageNumber);

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
    //=========================================
    public void generateFoldersAtSubscriberButton(long chatId, long messageId, User subscriber, User owner){
        int pageNumber = getCurrentPageNumber();

        EditMessageText message = new EditMessageText();
//        String folderName = folderRepository.findById(folderId).get().getName();
        String textForMes = "Папки, доступные пользователю @" + subscriber.getLink();

//        if(!eventInfo.isEmpty())
//            message.setText(textForMes + eventInfo);
//        else
        message.setText(textForMes);

        message.enableHtml(true);
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var goBackButton = new InlineKeyboardButton();
        String go_back_mark = EmojiParser.parseToUnicode(":back:");
        goBackButton.setText(go_back_mark);
        goBackButton.setCallbackData(CHECK_SUBSCRIBERS_BY_LINKS_BUTTON);

//        var addEventButton = new InlineKeyboardButton();
//        String add_folder_mark = EmojiParser.parseToUnicode(":heavy_plus_sign:");
//        addEventButton.setText(add_folder_mark + " папку");
//        addEventButton.setCallbackData();



        rowInline.add(goBackButton);
//        rowInline.add(addEventButton);
        rowsInline.add(rowInline);

        List<Folder> folderList =   getFolderListBySubscriberAndOwner(subscriber, owner);
        Iterator<Folder> folderIterator = folderList.iterator();
        var eventListSize = folderList.size();
        String delete_folder_mark = EmojiParser.parseToUnicode(":x:");
        String add_folder_mark = EmojiParser.parseToUnicode(":radio_button:");

        int curEventIndex = 0;

        while (folderIterator.hasNext() && curEventIndex < (pageNumber * 10)){
            folderIterator.next();
            curEventIndex++;
        }

        // задаю количество событий на странице
        int uppLimitEventIndex = curEventIndex + 10;
        System.out.println("temporaryIndexList" + temporaryIndexList);
        while (folderIterator.hasNext() && curEventIndex < uppLimitEventIndex) {
            rowInline = new ArrayList<>();

            Folder curFodler = folderIterator.next();

            var buttonFolderName = new InlineKeyboardButton();
            buttonFolderName.setText(curFodler.getName());
            buttonFolderName.setCallbackData(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON + subscriber.getLink());

            var buttonGreenCheckmark = new InlineKeyboardButton();
            if (!temporaryIndexList.contains((int)curFodler.getId()))
                buttonGreenCheckmark.setText(add_folder_mark);
            else
                buttonGreenCheckmark.setText(delete_folder_mark);
            buttonGreenCheckmark.setCallbackData(DELETE_FOLDER_FROM_PERMISSION_BUTTON + curFodler.getId());

            rowInline.add(buttonFolderName);
            rowInline.add(buttonGreenCheckmark);
            rowsInline.add(rowInline);
            curEventIndex++;
        }

//        var buttonDeleteFolder = new InlineKeyboardButton();
//        delete_folder_mark = EmojiParser.parseToUnicode("Удалить папку " + ":wastebasket:");
//        buttonDeleteFolder.setText(delete_folder_mark);
//        buttonDeleteFolder.setCallbackData(DELETE_FOLDER_BUTTON + folderId);
//        rowInline = new ArrayList<>();
//        rowInline.add(buttonDeleteFolder);
//        rowsInline.add(rowInline);



        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        if (pageNumber - 1 >= 0)
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + (pageNumber - 1));
        else
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + pageNumber);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((eventListSize / 10) + ((eventListSize % 10 == 0 && eventListSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)eventListSize / 10 > (pageNumber+1) )
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHECK_FOLDERS_FOR_SUBSCRIBER + (pageNumber + 1));
        else
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + pageNumber);

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
    public boolean generateDoYouWantSubscribe(long chatId, String ownerLink){

        if(userRepository.findById(chatId).isEmpty())
            return false;

        SendMessage message = new SendMessage();
        message.setText("Разрешить @" + ownerLink + " добавлять Вас в отслеживаемые события?");
        message.setChatId(chatId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData(YES_CONFIRM_BUTTON + ownerLink);

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData(ZERO_ACTION_BUTTON);

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

        return true;
    }
    public void generateChooseFoldersForSubscriberButton(long chatId, long messageId, User subscriber){
        int pageNumber = getCurrentPageNumber();

        EditMessageText message = new EditMessageText();
//        String folderName = folderRepository.findById(folderId).get().getName();
        String textForMes = "Ваши папки ";

//        if(!eventInfo.isEmpty())
//            message.setText(textForMes + eventInfo);
//        else
        message.setText(textForMes);

        message.enableHtml(true);
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var goBackButton = new InlineKeyboardButton();
        String go_back_mark = EmojiParser.parseToUnicode(":back:");
        goBackButton.setText(go_back_mark);
        goBackButton.setCallbackData(CHECK_SUBSCRIBERS_BY_LINKS_BUTTON);

        var saveButton = new InlineKeyboardButton();
//        String go_back_mark = EmojiParser.parseToUnicode(":back:");
        saveButton.setText("сохранить");
        saveButton.setCallbackData(ADD_FOLDER_WHICH_CHOOSE_TO_SUBSCRIBER_BUTTON);
//        var addEventButton = new InlineKeyboardButton();
//        String add_folder_mark = EmojiParser.parseToUnicode(":heavy_plus_sign:");
//        addEventButton.setText(add_folder_mark + " папку");
//        addEventButton.setCallbackData();



        rowInline.add(goBackButton);
        rowInline.add(saveButton);
//        rowInline.add(addEventButton);
        rowsInline.add(rowInline);
        User curUser = userRepository.findById(chatId).get();
        List<Folder> folderList = folderRepository.findByOwnerID(curUser);
        Iterator<Folder> folderIterator = folderList.iterator();
        var eventListSize = folderList.size();
        String delete_folder_mark = EmojiParser.parseToUnicode(":x:");
        String add_folder_mark = EmojiParser.parseToUnicode(":radio_button:");


        int curEventIndex = 0;

        while (folderIterator.hasNext() && curEventIndex < (pageNumber * 10)){
            folderIterator.next();
            curEventIndex++;
        }

        // задаю количество событий на странице
        int uppLimitEventIndex = curEventIndex + 10;

        //TODO доделать добавление папок
        while (folderIterator.hasNext() && curEventIndex < uppLimitEventIndex) {
            rowInline = new ArrayList<>();

            Folder curFodler = folderIterator.next();

            var buttonFolderName = new InlineKeyboardButton();
            buttonFolderName.setText(curFodler.getName());
            buttonFolderName.setCallbackData(CHECK_FOLDERS_FOR_SUBSCRIBER_BUTTON + subscriber.getLink());

            var buttonGreenCheckmark = new InlineKeyboardButton();
            if (!temporaryIndexList.contains((int)curFodler.getId()))
                buttonGreenCheckmark.setText(add_folder_mark);
            else
                buttonGreenCheckmark.setText(delete_folder_mark);
            buttonGreenCheckmark.setCallbackData(CHOOSE_FOLDER_FOR_SUBSCRIBER_BUTTON + curFodler.getId());

            rowInline.add(buttonFolderName);
            rowInline.add(buttonGreenCheckmark);
            rowsInline.add(rowInline);
            curEventIndex++;
        }

//        var buttonDeleteFolder = new InlineKeyboardButton();
//        delete_folder_mark = EmojiParser.parseToUnicode("Удалить папку " + ":wastebasket:");
//        buttonDeleteFolder.setText(delete_folder_mark);
//        buttonDeleteFolder.setCallbackData(DELETE_FOLDER_BUTTON + folderId);
//        rowInline = new ArrayList<>();
//        rowInline.add(buttonDeleteFolder);
//        rowsInline.add(rowInline);



        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        //TODO Кнопки поменять чтобы работали
        if (pageNumber - 1 >= 0)
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + (pageNumber - 1));
        else
            previousPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + pageNumber);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((pageNumber + 1) + "/" +
                ((eventListSize / 10) + ((eventListSize % 10 == 0 && eventListSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + pageNumber);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)eventListSize / 10 > (pageNumber+1) )
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON + IN_CHECK_FOLDERS_FOR_SUBSCRIBER + (pageNumber + 1));
        else
            nextPageButton.setCallbackData(SWITCH_PAGE_BUTTON+ IN_CHECK_FOLDERS_FOR_SUBSCRIBER + pageNumber);

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


    //===============
    // спросить
    private void generateTemplateButton(long chatId, String messageText,
                                        List<String> buttonTextList,
                                        List<String> buttonCallbackDataList
                                        ){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline        = new ArrayList<>();

        var checkSubscriptionsButton                = new InlineKeyboardButton();

        checkSubscriptionsButton.setText("Подписки");
        checkSubscriptionsButton.setCallbackData(CHECK_SUBSCRIPTIONS_BUTTON);

        var checkSubscribersButton = new InlineKeyboardButton();

        checkSubscribersButton.setText("Подписчики");
        checkSubscribersButton.setCallbackData(CHECK_SUBSCRIBERS_BUTTON);

        rowInline.add(checkSubscribersButton);
        rowInline.add(checkSubscriptionsButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error : " + e);
        }

    }




    //===============
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
        setLastCommand(chatId, MY_FOLDERS_COMMAND);


    }
    private void createAddCommand(long chatId){

        generateEventOrFolderButton(chatId);

    }
    private void createPermissionCommand(long chatId){
        generateSubscriptionsOrSubscribersButton(chatId);
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
            user.setLink(chat.getUserName());
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
    private void sendMessage(long chatId, long messageId, String textToSend){
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId((int) messageId);
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
