package com.yan.glengine.tester.screens;

import java.util.ArrayList;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import glengine.yan.glengine.assets.YANAssetManager;
import glengine.yan.glengine.nodes.YANButtonNode;
import glengine.yan.glengine.nodes.YANIRenderableNode;
import glengine.yan.glengine.nodes.YANTextNode;
import glengine.yan.glengine.nodes.YANTexturedNode;
import glengine.yan.glengine.renderer.YANGLRenderer;
import glengine.yan.glengine.screens.YANIScreen;
import glengine.yan.glengine.service.ServiceLocator;
import glengine.yan.glengine.tween.YANTweenNodeAccessor;
import glengine.yan.glengine.util.math.YANMathUtils;

/**
 * Created by Yan-Home on 1/18/2015.
 */
public class TweeningTestScreen extends BaseTestScreen {

    public static final int START_NODES_COUNT = 10;
    public static final int NODES_ADDITION_COUNT = 30;
    private TweenManager mTweenManager;
    private ArrayList<YANTexturedNode> mAnimateNodes;
    private TweenCallback mTweenCallback;
    private YANTextNode mNodesAmountText;
    private YANButtonNode mAddTexturesNode;

    public TweeningTestScreen(YANGLRenderer renderer) {
        super(renderer);
        mTweenManager = new TweenManager();
        mAnimateNodes = new ArrayList<>();

        //animation callback
        mTweenCallback = new TweenCallback() {
            @Override
            public void onEvent(int i, BaseTween<?> baseTween) {
                animateNode((YANIRenderableNode) baseTween.getUserData());
            }
        };
    }

    @Override
    public void onSetActive() {
        super.onSetActive();
        for (YANIRenderableNode node : mAnimateNodes) {
            animateNode(node);
        }
    }

    private void animateNode(YANIRenderableNode node) {
        float randomX = YANMathUtils.randomInRange(0, getSceneSize().getX());
        float randomY = YANMathUtils.randomInRange(0, getSceneSize().getY());
        float randomRotationZ = YANMathUtils.randomInRange(0, 360);

        Timeline.createSequence()
                .beginParallel()
                .push(Tween.to(node, YANTweenNodeAccessor.POSITION_X, 1.0f).target(randomX))
                .push(Tween.to(node, YANTweenNodeAccessor.POSITION_Y, 1.0f).target(randomY))
                .push(Tween.to(node, YANTweenNodeAccessor.ROTATION_Z_CW, 1.0f).target(randomRotationZ))
                .setUserData(node)
                .setCallback(mTweenCallback)
                .start(mTweenManager);
    }

    @Override
    public void onSetNotActive() {
        super.onSetNotActive();
        //remove all animations
        mTweenManager.killAll();
    }

    @Override
    protected void onLayoutNodes() {
        super.onLayoutNodes();
        mNodesAmountText.setPosition(50, 50);
    }

    @Override
    protected void onAddNodesToScene() {
        super.onAddNodesToScene();

        for (YANTexturedNode animatableNode : mAnimateNodes) {
            addNode(animatableNode);
        }

        //add text node
        addNode(mNodesAmountText);
        addNode(mAddTexturesNode);
    }

    @Override
    protected void onChangeNodesSize() {
        super.onChangeNodesSize();
        mAddTexturesNode.setSize(100, 100);
    }

    @Override
    protected void onCreateNodes() {
        super.onCreateNodes();
        for (int i = 0; i < START_NODES_COUNT; i++) {
            YANTexturedNode sprite = createSprite();
            mAnimateNodes.add(sprite);
        }

        //create a text node
        mNodesAmountText = new YANTextNode(ServiceLocator.locateService(YANAssetManager.class).getLoadedFont("standard_font"), "Nodes Amount 10000".length());
        mNodesAmountText.setText("Nodes Amount " + mAnimateNodes.size());
        mNodesAmountText.setSortingLayer(OVERLAY_SORTING_LAYER);

        //create add button
        mAddTexturesNode = new YANButtonNode(mUiAtlas.getTextureRegion("stump.png"), mUiAtlas.getTextureRegion("stump.png"));
        mAddTexturesNode.setSortingLayer(OVERLAY_SORTING_LAYER);
        mAddTexturesNode.setClickListener(new YANButtonNode.YanButtonNodeClickListener() {
            @Override
            public void onButtonClick() {
                for (int i = 0; i < NODES_ADDITION_COUNT; i++) {
                    YANTexturedNode sprite = createSprite();
                    mAnimateNodes.add(sprite);
                    addNode(sprite);
                    animateNode(sprite);
                }

                mNodesAmountText.setText("Nodes Amount " + mAnimateNodes.size());
            }
        });
    }

    private YANTexturedNode createSprite() {
        YANTexturedNode sprite = new YANTexturedNode(mUiAtlas.getTextureRegion("grey_cock.png"));
        sprite.setSize(100, 100);
        sprite.setPosition(YANMathUtils.randomInRange(0, getSceneSize().getX()), YANMathUtils.randomInRange(0, getSceneSize().getY()));
        return sprite;
    }

    @Override
    protected YANIScreen onSetNextScreen() {
        return new BlendingTestScreen(getRenderer());
    }

    @Override
    protected YANIScreen onSetPreviousScreen() {
        return new RotationsTestScreen(getRenderer());
    }

    @Override
    public void onUpdate(float deltaTimeSeconds) {
        super.onUpdate(deltaTimeSeconds);
        mTweenManager.update(deltaTimeSeconds);
    }
}
