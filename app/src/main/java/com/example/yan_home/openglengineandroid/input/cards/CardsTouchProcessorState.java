package com.example.yan_home.openglengineandroid.input.cards;

import com.yan.glengine.nodes.YANTexturedNode;

/**
 * Created by Yan-Home on 11/21/2014.
 */
public abstract class CardsTouchProcessorState {
    public abstract void onTouchUp();

    public abstract void onCardTouchDrag(YANTexturedNode touchedCard);

    public abstract void onCardTouchDown(YANTexturedNode touchedCard);
}
