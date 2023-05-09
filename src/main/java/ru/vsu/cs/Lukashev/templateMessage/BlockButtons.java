package ru.vsu.cs.Lukashev.templateMessage;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class BlockButtons {
    private List<LineButtons> listLineButtons;


    public BlockButtons() {
        this.listLineButtons = new ArrayList<>();
    }

    public List<List<InlineKeyboardButton>> generate(){
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for(LineButtons l : listLineButtons){
            rows.add(l.getLineButtons());
        }
        return rows;
    }



    public BlockButtons add(LineButtons lineButtons){
        listLineButtons.add(lineButtons);
        return this;
    }
}
