package com.fang.example.spring.di;

/**
 * Created by andy on 4/3/16.
 */
public class BraveKnight implements Knight  {
    private Quest _quest;
    public BraveKnight(Quest quest) {
        _quest = quest;
    }
    public void embarkOnquest() {
        _quest.embark();
    }
}
