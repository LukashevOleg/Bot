package ru.vsu.cs.Lukashev.templateMessage;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LineSwitchButtons extends LineButtons{

    private int currentPage;
    private int listObjectSize;

    public LineSwitchButtons(List<String> listCallBackData, int currentPage, int listObjectSize) {
        super(null, listCallBackData);
        this.currentPage = currentPage;
        this.listObjectSize = listObjectSize;
    }

    @Override
    public List<InlineKeyboardButton> getLineButtons() {
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
//        Iterator<String> textInButtonIterator = getListTextInButton().iterator();
        Iterator<String> callbackDataIterator = getListCallBackData().iterator();

//        while (callbackDataIterator.hasNext() && textInButtonIterator.hasNext()) {
//            inlineKeyboardButtons.add(createButton(textInButtonIterator.next(), callbackDataIterator.next()));
//        }

        var previousPageButton = new InlineKeyboardButton();
        String arrow_left = EmojiParser.parseToUnicode(":arrow_left:");
        previousPageButton.setText(arrow_left);

        String page = callbackDataIterator.next();

        if (currentPage - 1 >= 0)
            previousPageButton.setCallbackData(page + (currentPage - 1));
        else
            previousPageButton.setCallbackData(page + currentPage);
        inlineKeyboardButtons.add(previousPageButton);

        var infoWhichPageButton = new InlineKeyboardButton();
        infoWhichPageButton.setText((currentPage + 1) + "/" +
                ((listObjectSize / 10) + ((listObjectSize % 10 == 0 && listObjectSize > 10) ? 0 : 1)));
        infoWhichPageButton.setCallbackData(page + currentPage);
        inlineKeyboardButtons.add(infoWhichPageButton);

        var nextPageButton = new InlineKeyboardButton();
        String arrow_right = EmojiParser.parseToUnicode(":arrow_right:");
        nextPageButton.setText(arrow_right);
        //проверяем есть ли другие страницы
        if ((double)listObjectSize / 10 > (currentPage +1) )
            nextPageButton.setCallbackData(page + (currentPage + 1));
        else
            nextPageButton.setCallbackData(page + currentPage);
        inlineKeyboardButtons.add(nextPageButton);

        return inlineKeyboardButtons;
//        return super.getLineButtons();
    }
}
