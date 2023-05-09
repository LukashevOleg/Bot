package ru.vsu.cs.Lukashev.templateMessage;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter @Setter
public class LineButtons {

    private List<String> listTextInButton;
    private List<String> listCallBackData;

    public LineButtons(List<String> listTextInButtonList, List<String> listCallBackData) {
        this.listTextInButton = listTextInButtonList;
        this.listCallBackData = listCallBackData;
    }

    public LineButtons() {
    }


    public List<InlineKeyboardButton> getLineButtons(){
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        Iterator<String> textInButtonIterator = listTextInButton.iterator();
        Iterator<String> callbackDataIterator = listCallBackData.iterator();

        while (callbackDataIterator.hasNext() && textInButtonIterator.hasNext()) {
            inlineKeyboardButtons.add(createButton(textInButtonIterator.next(), callbackDataIterator.next()));
        }

        return inlineKeyboardButtons;
    }

    protected InlineKeyboardButton createButton(String text, String callbackData){
        var button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
