package com.example.yan_home.openglengineandroid.input.cards.states;

import com.example.yan_home.openglengineandroid.input.cards.CardsTouchProcessorState;
import com.yan.glengine.nodes.YANTexturedNode;

/**
 * Created by Yan-Home on 11/21/2014.
 */
public class CardsTouchProcessorDefaultState extends CardsTouchProcessorState {
    @Override
    public void onTouchUp() {

    }

    @Override
    public void onCardTouchDrag(YANTexturedNode touchedCard) {

    }

    @Override
    public void onCardTouchDown(YANTexturedNode touchedCard) {
        touchedCard.setSize(touchedCard.getSize().getX() * 1.2f, touchedCard.getSize().getY() * 1.2f);
    }
}
