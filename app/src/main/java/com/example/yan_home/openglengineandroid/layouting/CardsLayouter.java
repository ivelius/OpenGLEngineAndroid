package com.example.yan_home.openglengineandroid.layouting;

import java.util.List;

/**
 * Created by Yan-Home on 11/8/2014.
 */
public interface CardsLayouter {

    void setActiveSlotsAmount(int amount);

    /**
     * Sets required parameters for layouter to do the calculations
     *
     * @param cardWidth          width of the card on the screen
     * @param cardHeight         height of the card on the screen
     * @param maxAvailableWidth  maximum width that is available for cards to position
     * @param maxAvailableHeight maximum height that is available for cards to position
     * @param baseXPosition      starting x point fron where cards will be layed out
     * @param baseYPosition      starting y point from the bottom of the screen , where bottom cards line will be.
     */
    void init(float cardWidth, float cardHeight, float maxAvailableWidth, float maxAvailableHeight,float baseXPosition, float baseYPosition);

    CardsLayoutSlot getSlotAtPosition(int position);

    List<List<Integer>> getSequences();

}