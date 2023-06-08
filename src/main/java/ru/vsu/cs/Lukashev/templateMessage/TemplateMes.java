package ru.vsu.cs.Lukashev.templateMessage;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Setter @Getter
public class TemplateMes {

    private List<BlockButtons> blockButtonsList;

    private List<List<InlineKeyboardButton>> bodyButtonsList;
    private EditMessageText editMessage;
    private SendMessage sendMessage;

    public TemplateMes() {
        this.bodyButtonsList = new ArrayList<>();
        this.blockButtonsList = new ArrayList<>();
    }

    public TemplateMes create(Long chatId, int messageId, String text){

        this.editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.enableHtml(true);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        Iterator<BlockButtons> blockButtonsIterator = blockButtonsList.iterator();
        if(!bodyButtonsList.isEmpty())
            rowsInline.addAll(blockButtonsIterator.next().generate());
        rowsInline.addAll(bodyButtonsList);

        while (blockButtonsIterator.hasNext())
            rowsInline.addAll(blockButtonsIterator.next().generate());

        markupInline.setKeyboard(rowsInline);
        editMessage.setReplyMarkup(markupInline);
        return this;
    }

    public EditMessageText getEditMessage() {
        return editMessage;
    }

    public SendMessage getSendMessage(){
        return sendMessage;
    }


    public TemplateMes create(Long chatId, String text){
        this.sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        InlineKeyboardMarkup markupInline           = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        for(BlockButtons block : blockButtonsList)
            rowsInline.addAll(block.generate());

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);


        return this;
    }

    public TemplateMes addBlock(BlockButtons blockButtons){
        this.blockButtonsList.add(blockButtons);

        return this;
    }



}
